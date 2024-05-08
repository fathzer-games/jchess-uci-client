package com.fathzer.uci.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.io.CharSource;

class UCIEngineBaseTest {

	@Test
	void goTest() throws Exception {
		final StringBuilder r = new StringBuilder();
		final StringWriter w = new StringWriter();
		r.append("id name toto\nuciok\n");
		try (UCIEngineBase eng = new UCIEngineBase(CharSource.wrap(r).openBufferedStream() , new BufferedWriter(w), CharSource.wrap(new StringBuilder()).openBufferedStream())) {
			assertEquals("uci\n", toUnix(w.toString()));
			w.getBuffer().delete(0, w.getBuffer().length());

			final CountDownState time = new CountDownState(0, 0, 0);
			assertThrows(IllegalStateException.class, () -> eng.go(time));
			
			eng.setPosition(Optional.empty(), Collections.emptyList());
			assertEquals("position startpos\n", toUnix(w.toString()));
			w.getBuffer().delete(0, w.getBuffer().length());

			r.append("bestmove d2d4");
			GoReply reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());
			assertEquals("go\n", toUnix(w.toString()));
			w.getBuffer().delete(0, w.getBuffer().length());
		}
	}

	private String toUnix(String string) {
		return string.replaceAll("\\r", "");
	}
	
}
