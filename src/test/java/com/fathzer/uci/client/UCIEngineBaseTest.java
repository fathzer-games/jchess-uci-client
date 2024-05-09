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
			assertEquals("toto", eng.getName());
			assertEquals(Arrays.asList("Hash"), eng.getOptions().stream().map(Option::getName).toList());

			eng.onReceive("setoption name Hash value 16", Collections.emptyList());
			final Option<?> option = eng.getOptions().stream().filter(o -> "Hash".equals(o.getName())).findFirst().get();
			Option.setValue(option, "16");
			assertTrue(eng.isReceivedConsumed());
			
			assertThrows(IllegalStateException.class, () -> eng.go(null));
			
			eng.onReceive("position startpos");
			eng.setPosition(Optional.empty(), Collections.emptyList());

			eng.onReceive("go", Collections.singletonList("bestmove d2d4"));
			GoReply reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());

			eng.onReceive("go", Collections.singletonList("bestmove d2d4 ponder d7d5"));
			reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());
			assertTrue(reply.getMoves().get(0).getScore().isEmpty());
			assertEquals("d7d5", reply.getPonderMove().get());
			
			eng.onReceive("go", Arrays.asList("info depth 7 seldepth 6 multipv 1 score upperbound 25 nodes 2039 nps 291285 hashfull 1 tbhits 0 time 7 pv e2e4 d7d5 e4d5 d8d5 g1f3",
					"info depth 7 seldepth 7 multipv 2 score lowerbound 25 nodes 2039 nps 291285 hashfull 1 tbhits 0 time 7 pv g1f3 g8f6 e2e3 a7a6 c2c4",
					"info depth 8 seldepth 7 multipv 1 score cp 28 nodes 8133 nps 677750 hashfull 4 tbhits 0 time 12 pv d2d4 e7e6 b1c3 c7c5",
					"info depth 8 seldepth 12 multipv 2 score mate -5 nodes 8133 nps 677750 hashfull 4 tbhits 0 time 12 pv e2e4 e7e6 g1f3 d7d5",
					"bestmove d2d4 ponder e7e6"));
			reply = eng.go(null);
			assertEquals("d2d4", reply.getMoves().get(0).getMove());
			assertEquals("e7e6", reply.getPonderMove().get());
			assertEquals(new GoReply.CpScore(28), reply.getMoves().get(0).getScore().get());
			assertEquals(Arrays.asList("d2d4","e7e6","b1c3","c7c5"), reply.getMoves().get(0).getPv().get());
			assertEquals(new GoReply.MateScore(-5), reply.getMoves().get(1).getScore().get());
			assertEquals(8, reply.getMoves().get(1).getDepth().getAsInt());
			assertEquals(12, reply.getMoves().get(1).getSelDepth().getAsInt());
			
			eng.onReceive("go", Arrays.asList("info depth 6 nodes 14468","info depth 7 nodes 53726",
					"info score cp 25 depth 7 nodes 248442 time 0 pv b1c3 e7e5 g1f3 b8c6 e2e3 f8b4 f1c4",
					"bestmove b1c3"));
			reply = eng.go(null);
			assertEquals("b1c3", reply.getMoves().get(0).getMove());
			assertTrue(reply.getPonderMove().isEmpty());
			assertEquals(new GoReply.CpScore(25), reply.getMoves().get(0).getScore().get());

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
			onReceive("uci", Arrays.asList("id name toto","option name Hash type spin default 1 min 1 max 512","uciok"));
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
		public boolean isReceivedConsumed() {
			return this.expectedRequest==null;
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
