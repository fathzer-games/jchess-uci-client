package com.fathzer.uci.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.io.CharSource;

class UCIEngineBaseTest {
	
	@Test
	void test() throws Exception {
		try (TestEngine eng = TestEngine.build()) {

			final CountDownState time = new CountDownState(0, 0, 0);
			assertThrows(IllegalStateException.class, () -> eng.go(time));
			
			assertEquals("toto", eng.getName());
			assertTrue(eng.getOptions().isEmpty());
			
			eng.onReceive("position startpos");
			eng.setPosition(Optional.empty(), Collections.emptyList());

			eng.onReceive("go", Collections.singletonList("bestmove d2d4"));
			GoReply reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());

			eng.onReceive("go", Collections.singletonList("bestmove d2d4 ponder d7d5"));
			reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());
			assertEquals("d7d5", reply.getPonderMove().get());

			eng.onReceive("quit");
		}
	}

	private static class TestEngine extends UCIEngineBase {
		private final StringBuilder r;
		private String expectedRequest;
		private List<String> reply;
		
		private TestEngine(StringBuilder r, StringWriter w) throws IOException {
			super(CharSource.wrap(r).openBufferedStream(), new BufferedWriter(w));
			this.r = r;
			onReceive("uci", Arrays.asList("id name toto","uciok"));
			init();
		}

		static TestEngine build() throws IOException {
			return new TestEngine(new StringBuilder(), new StringWriter());
		}
		
		public void onReceive(String request) {
			this.expectedRequest = request;
			this.reply = Collections.emptyList();
		}
		public void onReceive(String request, List<String> reply) {
			this.expectedRequest = request;
			this.reply = reply;
		}

		@Override
		protected void write(String line) throws IOException {
			assertEquals(expectedRequest, line);
			expectedRequest = null;
			reply.forEach(s -> {
				r.append(s);
				r.append('\n');
			});
			super.write(line);
		}
	}
}
