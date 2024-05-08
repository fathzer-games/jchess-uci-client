package com.fathzer.uci.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathzer.uci.client.GoReply.UCIMove;
import com.fathzer.uci.client.Option.Type;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

class UCIEngineBase implements Closeable {
	private static final String ID_NAME_PREFIX = "id name ";
	private static final String CHESS960_OPTION = "UCI_Chess960";
	private static final String PONDER_OPTION = "Ponder";
	
	static class StdErrReader implements Closeable, Runnable {
		private final BufferedReader errorReader;
		private final Thread spyThread;
		private final AtomicBoolean stopped;

		StdErrReader(BufferedReader errorReader) {
			this.errorReader = errorReader;
			this.stopped = new AtomicBoolean();
			this.spyThread = new Thread(this);
			this.spyThread.setDaemon(true);
			this.spyThread.start();
		}
		
		@Override
		public void run() {
			while (!stopped.get()) {
				try {
					final String line = errorReader.readLine();
					if (line!=null) {
						System.err.println (line); //TODO
					}
				} catch (EOFException e) {
					if (!stopped.get()) {
						log(e);
					}
				} catch (IOException e) {
					log(e);
				}
			}
		}

		private void log(IOException e) {
			synchronized(System.err) {
				System.err.println("An error occured, stopped is "+ stopped);
				e.printStackTrace(); //TODO
			}
		}

		@Override
		public void close() throws IOException {
			this.stopped.set(true);
			this.errorReader.close();
		}
	}
	private static final Logger log = LoggerFactory.getLogger(UCIEngineBase.class);
	
	private String name;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final StdErrReader errorReader;
	private final List<Option<?>> options;
	private boolean is960Supported;
	private boolean whiteToPlay;
	private boolean positionSet;

	UCIEngineBase(BufferedReader reader, BufferedWriter writer, BufferedReader errorReader) throws IOException {
		this.reader = reader;
		this.writer = writer;
		this.errorReader = new StdErrReader(errorReader);
		this.options = new ArrayList<>();
		init();
	}
	
	private void init() throws IOException {
		this.write("uci");
		String line;
		do {
			line = read();
			if (line==null) {
				throw new EOFException();
			} else if (line.startsWith(ID_NAME_PREFIX)) {
				name = line.substring(ID_NAME_PREFIX.length());
			} else {
				final Optional<Option<?>> ooption = parseOption(line);
				if (ooption.isPresent() && isOptionSupported(ooption.get())) {
					options.add(ooption.get());
				}
			}
		} while (!"uciok".equals(line));
	}
	
	private boolean isOptionSupported(Option<?> option) {
		if (CHESS960_OPTION.equals(option.getName())) {
			is960Supported = true;
		} else if (!PONDER_OPTION.equals(option.getName())) {
			//TODO Ponder is not supported yet
			option.addListener((prev, cur) -> {
				try {
					setOption(option, cur);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			return true;
		}
		return false;
	}
	
	private Optional<Option<?>> parseOption(String line) throws IOException {
		try {
			return OptionParser.parse(line);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
	}
	
	private void setOption(Option<?> option, Object value) throws IOException {
		final StringBuilder buf = new StringBuilder("setoption name ");
		buf.append(option.getName());
		if (Type.BUTTON!=option.getType()) {
			buf.append(" value ");
			buf.append(value);
		}
		write(buf.toString());
	}
	
	private void write(String line) throws IOException {
		this.writer.write(line);
		this.writer.newLine();
		this.writer.flush();
		log.info(">{}: {}", getName(), line);
	}
	private String read() throws IOException {
		final String line = reader.readLine();
		log.info("<{} : {}", getName(), line);
		return line;
	}

	public List<Option<?>> getOptions() {
		return options;
	}
	
	public boolean isSupported(Variant variant) {
		return variant==Variant.STANDARD || (variant==Variant.CHESS960 && is960Supported);
	}

	public boolean newGame(Variant variant) throws IOException {
		positionSet = false;
		if (variant==Variant.CHESS960 && !is960Supported) {
			return false;
		}
		write("ucinewgame");
		if (is960Supported) {
			write("setoption name "+CHESS960_OPTION + " value "+(variant==Variant.CHESS960));
		}
		write("isready");
		return waitAnswer("readyok"::equals)!=null;
	}

	/** Reads the engine standard output until a valid answer is returned.
	 * @param answerValidator a predicate that checks the lines returned by engine. 
	 * @return The line that is considered valid, null if no valid line is returned
	 * and the engine closed its output.
	 * @throws IOException If communication with engine fails
	 */
	private String waitAnswer(Predicate<String> answerValidator) throws IOException {
		for (String line = read(); line!=null; line=read()) {
			if (answerValidator.test(line)) {
				return line;
			}
		}
		throw new EOFException();
	}

	public void setPosition(Optional<String> fen, List<String> moves) throws IOException {
		whiteToPlay = fen.isEmpty() || "w".equals(fen.get().split(" ")[1]);
		if (moves.size()%2!=0) {
			whiteToPlay = !whiteToPlay;
		}
		final StringBuilder builder = new StringBuilder(fen.isEmpty() ? "position startpos" : "position fen "+fen.get());
		if (!moves.isEmpty()) {
			builder.append(" moves");
			for (String move : moves) {
				builder.append(" ");
				builder.append(move);
			}
		}
		write(builder.toString());
		positionSet = true;
	}

	public GoReply go(CountDownState params) throws IOException {
		if (!positionSet) {
			throw new IllegalStateException("No position defined");
		}
		final StringBuilder command = new StringBuilder("go");
		if (params!=null) {
			command.append(' ');
			final char prefix = whiteToPlay ? 'w' : 'b';
			command.append(prefix);
			command.append("time ");
			command.append(params.remainingMs());
			if (params.incrementMs()>0) {
				command.append(" ");
				command.append(prefix);
				command.append("inc ");
				command.append(params.incrementMs());
			}
			if (params.movesToGo()>0) {
				command.append(" ");
				command.append("movestogo ");
				command.append(params.movesToGo());
			}
		}
		write (command.toString());
		var bestMovePrefix = "bestmove ";
		final String answer = waitAnswer(s -> s.startsWith(bestMovePrefix));
		return new GoReply(new UCIMove(answer.substring(bestMovePrefix.length())));
	}

	public void close() throws IOException {
		this.write("quit");
		closeStreams();
	}

	public void closeStreams() throws IOException {
		this.reader.close();
		this.writer.close();
		this.errorReader.close();
	}

	public String getName() {
		return name;
	}
}
