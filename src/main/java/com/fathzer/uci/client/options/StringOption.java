package com.fathzer.uci.client.options;

import com.fathzer.uci.client.Option;

public class StringOption extends Option<String> {
	public StringOption(String name, String defaultValue) {
		super(name, defaultValue);
		if (defaultValue==null) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Type getType() {
		return Type.STRING;
	}

	@Override
	public boolean isValid(String value) {
		return value!=null;
	}

	@Override
	public String toValue(String value) {
		return value;
	}
}
