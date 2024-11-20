package com.fathzer.uci.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathzer.games.util.exec.CustomThreadFactory;
import com.fathzer.games.util.exec.ItemPublisher;
import com.fathzer.games.util.exec.ItemPublisher.ItemListener;

class InterruptibleReplyListener implements AutoCloseable {
	private static final Logger LOG = LoggerFactory.getLogger(InterruptibleReplyListener.class);
	
	@FunctionalInterface
	interface InternalWriter {
		void writeLine(String line) throws IOException;
	}

	@FunctionalInterface
	interface InternalReader {
		String readLine() throws IOException;
	}

	private static record ExceptionOrLine(String line, IOException e) {}
	
	private static final class ReplyBuilder implements ItemListener<ExceptionOrLine> {
		private final AtomicReference<ExceptionOrLine> result;
		private final Consumer<String> otherLines;
		private final Predicate<String> answerValidator;

		private ReplyBuilder(Consumer<String> otherLines, Predicate<String> answerValidator) {
			this.result = new AtomicReference<>();
			this.otherLines = otherLines;
			this.answerValidator = answerValidator;
		}

		@Override
		public void accept(ExceptionOrLine eol) {
			final String line = eol.line();
			if (line!=null) {
				if (answerValidator.test(line)) {
					result.set(eol);
					synchronized (this) {
						this.notifyAll();
					}
				} else {
					otherLines.accept(line);
				}
			} else {
				result.set(eol);
			}
		}
	}

	private class ReplyListener implements Runnable {
		@Override
		public void run() {
			LOG.trace("ReplyListener started");
			try {
				for (String line=reader.readLine(); line!=null; line=reader.readLine()) {
					if (linePublisher.getSubscribersCount()>0) {
						LOG.trace("Received {}", line);
						linePublisher.submit(new ExceptionOrLine(line, null));
					}
				}
				throw new EOFException();
			} catch (IOException e) {
				if (linePublisher.getSubscribersCount()>0) {
					LOG.trace("Received exception {}", e);
					linePublisher.submit(new ExceptionOrLine(null, e));
				}
			}
			LOG.trace("ReplyListener ended");
		}
	}
	
	private final InternalWriter writer;
	private final InternalReader reader;
	private final ItemPublisher<ExceptionOrLine> linePublisher;
	
	InterruptibleReplyListener(InternalWriter writer, InternalReader reader) {
		this.writer = writer;
		this.reader = reader;
		this.linePublisher = new ItemPublisher<>(null);
		ThreadFactory tf = new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("InterruptibleBufferedReader.internalListener"), true);
		tf = new CustomThreadFactory(new CustomThreadFactory.BasicThreadNameSupplier("InterruptibleBufferedReader"), true);
		tf.newThread(linePublisher).start();
		LOG.trace("Publisher started");
		tf.newThread(new ReplyListener()).start();
	}
	
	String waitAnswer(String command, Predicate<String> answerValidator, Consumer<String> otherLines) throws IOException {
		final ReplyBuilder listener = new ReplyBuilder(otherLines, answerValidator);
		linePublisher.subscribe(listener);
		try {
			LOG.trace("Subscription for {} done", command);
			writer.writeLine(command);
			while (listener.result.get()==null) {
				synchronized (listener) {
					listener.wait();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InterruptedIOException();
		} finally {
			linePublisher.unsubscribe(listener);
		}
		final ExceptionOrLine eol = listener.result.get();
		if (eol.e()!=null) {
			throw eol.e();
		} else {
			return eol.line();
		}
	}

	@Override
	public void close() throws IOException {
		this.linePublisher.close();
	}
}
