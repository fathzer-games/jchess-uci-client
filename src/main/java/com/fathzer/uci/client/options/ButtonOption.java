package com.fathzer.uci.client.options;

import com.fathzer.uci.client.Option;

public class ButtonOption extends Option<Void> {
	
	public ButtonOption(String name) {
		super(name, null);
	}
	
	@Override
	public Type getType() {
		return Type.BUTTON;
	}

	@Override
	public boolean isValid(Void value) {
		return true;
	}

	@Override
	public void setValue(Void value) {
		super.fireChange(value, value);
	}

	@Override
	public Void toValue(String value) {
		return null;
	}
}
