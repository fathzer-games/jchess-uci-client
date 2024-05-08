package com.fathzer.uci.client;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

public class UCIEngine implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(UCIEngine.class);
	
	private final Process process;
	private final List<String> command;
	private final UCIEngineBase uciBase;
	private boolean expectedRunning;

	
	public UCIEngine(ProcessBuilder builder) throws IOException {
		this.command = builder.command();
		log.info ("Launching uci engine with command {}", command);
		this.process = builder.start();
		log.info("Engine launched with {} process id is {}", command, process.pid());
		this.expectedRunning = true;
		uciBase = new UCIEngineBase(process.inputReader(), process.outputWriter(), process.errorReader());
		process.onExit().thenAccept(p -> {
			if (expectedRunning) {
				expectedRunning = false;
				log.warn("{} UCI engine exited unexpectedly with code {}", command, p.exitValue());
				try {
					uciBase.closeStreams();
				} catch (IOException e) {
					log.error("The following error occured while closing streams of {}", command);
				}
			} else {
				log.info("{} UCI engine exited with code {}", command, p.exitValue());
			}
		});
	}
	
	public UCIEngine(List<String> command) throws IOException {
		this(new ProcessBuilder(command));
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

	public GoReply go(CountDownState params) throws IOException {
		return uciBase.go(params);
	}

	@Override
	public void close() throws IOException {
		if (!expectedRunning) {
			return;
		}
		expectedRunning = false;
		uciBase.close();
		try {
			this.process.waitFor(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Fail to gracefully close UCI engine {}, trying to destroy it", getName());
			this.process.destroy();
			Thread.currentThread().interrupt();
		}
	}

	public String getName() {
		return uciBase.getName();
	}
}
