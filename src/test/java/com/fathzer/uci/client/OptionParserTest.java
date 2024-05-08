package com.fathzer.uci.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.fathzer.uci.client.options.ComboOption;
import com.fathzer.uci.client.options.SpinOption;
import com.fathzer.uci.client.options.StringOption;

class OptionParserTest {

	@Test
	void test() {
		
		Optional<Option<?>> ooption = OptionParser.parse("option name test type spin min 0 max 10 default 5");
		assertTrue(ooption.isPresent());
		assertEquals(SpinOption.class, ooption.get().getClass());
		SpinOption spin = (SpinOption) ooption.get();
		assertEquals("test", spin.getName());
		assertEquals(5, spin.getValue());
		assertEquals(0, spin.getMin());
		assertEquals(10, spin.getMax());
		
		ooption = OptionParser.parse("option name Book Directory type string default C:\\Program Files (x86)\\Arena\\Engines\\Dragon");
		assertTrue(ooption.isPresent());
		Option<?> option = ooption.get();
		assertEquals(StringOption.class, option.getClass());
		assertEquals("Book Directory", option.getName());
		assertEquals("C:\\Program Files (x86)\\Arena\\Engines\\Dragon", option.getValue());

		ooption = OptionParser.parse("option name x default a b var c var a b type combo");
		assertTrue(ooption.isPresent());
		ComboOption combo = (ComboOption) ooption.get();
		assertEquals("x", combo.getName());
		assertEquals("a b", combo.getValue());
		assertTrue(combo.getValues().contains("c"));
	}

}
