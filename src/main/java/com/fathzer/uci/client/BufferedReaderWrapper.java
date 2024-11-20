package com.fathzer.uci.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class InputStreamWrapper extends FilterInputStream {

	protected InputStreamWrapper(InputStream in) {
		super(in);
	}

	@Override
	public void close() throws IOException {
		// Does not close the enclosing stream
	}
}
