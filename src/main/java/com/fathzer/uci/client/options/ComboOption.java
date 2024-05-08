package com.fathzer.uci.client.options;

import java.util.Set;

import com.fathzer.uci.client.Option;

public class ComboOption extends Option<String> {
	private final Set<String> values;
	
	public ComboOption(String name, String defaultValue, Set<String> values) {
		super(name, defaultValue);
		if (defaultValue==null || !values.contains(defaultValue)) {
			throw new IllegalArgumentException();
		}
		this.values = values;
	}

	public Set<String> getValues() {
		return values;
	}

	@Override
	public Type getType() {
		return Type.COMBO;
	}

	@Override
	public boolean isValid(String value) {
		return value!=null && values.contains(value);
	}

	@Override
	public String toValue(String value) {
		return value;
	}
}
