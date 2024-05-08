package com.fathzer.uci.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UCIEngineTest {

//	@Test
	void test() throws Exception {
		try (final var engine = new UCIEngine(Arrays.asList("C:/Program Files (x86)/Arena/Engines/Dragon/Dragon_46.exe"))) {
			assertEquals("Dragon 4.6", engine.getName());
//			synchronized(this) {
//				wait(600000);
//			}
		}
	}

}
