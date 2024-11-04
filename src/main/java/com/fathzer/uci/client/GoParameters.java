package com.fathzer.uci.client;

import java.util.LinkedList;
import java.util.List;

/** The arguments of the <i>go</i> UCI command.
 */
public class GoParameters {
	public static class TimeControl {
		private long remainingMs = -1;
		private long incrementMs;
		private int movesToGo;

		public long getRemainingMs() {
			return remainingMs;
		}

		public void setRemainingMs(long remainingMs) {
			this.remainingMs = remainingMs;
		}

		public long getIncrementMs() {
			return incrementMs;
		}

		public void setIncrementMs(long incrementMs) {
			this.incrementMs = incrementMs;
		}

		public int getMovesToGo() {
			return movesToGo;
		}

		public void setMovesToGo(int movesToGo) {
			this.movesToGo = movesToGo;
		}
	}

	private TimeControl timeControl = new TimeControl();
	private boolean ponder;
	private int depth = 0;
	private int nodes = 0;
	private int mate = 0;
	private List<String> moveToSearch = new LinkedList<>();
	private int moveTimeMs;
	private boolean infinite; 

	/** Gets the <i>depth</i> option.
	 * @return 0 if the option is not set
	 */
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/** Gets the <i>nodes</i> option.
	 * @return 0 if the option is not set
	 */
	public int getNodes() {
		return nodes;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	/** Gets the <i>mate</i> option.
	 * @return 0 if the option is not set
	 */
	public int getMate() {
		return mate;
	}

	public void setMate(int mate) {
		this.mate = mate;
	}

	public boolean isPonder() {
		return ponder;
	}

	public void setPonder(boolean ponder) {
		this.ponder = ponder;
	}

	public List<String> getMoveToSearch() {
		return moveToSearch;
	}

	public int getMoveTimeMs() {
		return moveTimeMs;
	}

	public void setMoveTimeMs(int moveTimeMs) {
		this.moveTimeMs = moveTimeMs;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}

	public TimeControl getTimeControl() {
		return timeControl;
	}
}
