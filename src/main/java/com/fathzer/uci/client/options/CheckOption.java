package com.fathzer.uci.client.options;

import com.fathzer.uci.client.Option;

public class CheckOption extends Option<Boolean> {
	public CheckOption(String name, boolean defaultValue) {
		super(name, defaultValue);
	}

	@Override
	public Type getType() {
		return Type.CHECK;
	}

	@Override
	public boolean isValid(Boolean value) {
		return value!=null;
	}

	@Override
	public Boolean toValue(String value) {
		return Boolean.valueOf(value);
	}
}
