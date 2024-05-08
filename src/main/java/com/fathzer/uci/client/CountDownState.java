package com.fathzer.uci.client;

/** Warning this class will change for sure!!!
 * It is not generic enough to reflect every clock state that can happen.
 * Probably, it should contains a clock settings view
 */
public record CountDownState(long remainingMs, long incrementMs, int movesToGo) {
	public CountDownState {
		if (movesToGo<0) {
			throw new IllegalArgumentException();
		}
	}
}
