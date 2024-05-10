package com.fathzer.uci.client;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;

public class UCIEngine implements Closeable {
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
						log.warn(line, line); //TODO
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

	private static final Logger log = LoggerFactory.getLogger(UCIEngine.class);
	
	private final Process process;
	private final List<String> command;
	private final UCIEngineBase uciBase;
	private final StdErrReader errorReader;
	private boolean expectedRunning;
	
	public UCIEngine(ProcessBuilder builder, Consumer<UCIEngine> preInit) throws IOException {
		this.command = builder.command();
		log.info("Launching uci engine with command {}", command);
		this.process = builder.start();
		log.info("Engine launched with {} process id is {}", command, process.pid());
		this.expectedRunning = true;
		uciBase = new UCIEngineBase(process.inputReader(), process.outputWriter()) {
			@Override
			protected void write(String line) throws IOException {
				super.write(line);
				log(line, false);
			}

			@Override
			protected String read() throws IOException {
				final String line = super.read();
				log(line, true);
				return line;
			}
		};
		this.errorReader = new StdErrReader(process.errorReader());
		process.onExit().thenAccept(p -> {
			if (expectedRunning) {
				expectedRunning = false;
				log.warn("{} UCI engine exited unexpectedly with code {}", command, p.exitValue());
				try {
					uciBase.closeStreams();
					this.errorReader.close();
				} catch (IOException e) {
					log.error("The following error occured while closing streams of {}", command);
				}
			} else {
				log.info("{} UCI engine exited with code {}", command, p.exitValue());
			}
		});
		if (preInit!=null) {
			preInit.accept(this);
		}
		uciBase.init();
	}
	
	public UCIEngine(List<String> command, Consumer<UCIEngine> preInit) throws IOException {
		this(new ProcessBuilder(command), preInit);
	}

	public List<Option<?>> getOptions() {
		return uciBase.getOptions();
	}
	
	public boolean isSupported(Variant variant) {
		return uciBase.isSupported(variant);
	}

	public boolean newGame(Variant variant) throws IOException {
		return uciBase.newGame(variant);
	}

	public void setPosition(Optional<String> fen, List<String> moves) throws IOException {
		uciBase.setPosition(fen, moves);
	}

	public GoReply go(GoParameters params) throws IOException {
		return uciBase.go(params);
	}

	@Override
	public void close() throws IOException {
		if (!expectedRunning) {
			return;
		}
		expectedRunning = false;
		uciBase.close();
		this.errorReader.close();
		try {
			this.process.waitFor(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Fail to gracefully close UCI engine {}, trying to destroy it", getName());
			this.process.destroy();
			Thread.currentThread().interrupt();
		}
	}
	
	protected void log(String line, boolean from) {
		log.info("{}{} : {}", from ? "<":">", getName(), line);
	}

	public String getName() {
		return uciBase.getName();
	}
}
