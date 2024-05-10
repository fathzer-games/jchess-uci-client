package com.fathzer.uci.client;

import java.util.LinkedList;
import java.util.List;

/** The arguments of the <i>go</i> UCI command.
 */
public class GoParameters {
//	private static final ParamProperties<GoParameters> WTIME_PARAM = new ParamProperties<>((p,tok) -> p.time.whiteClock.remainingMs=Parser.positiveInt(tok), "wtime");
//	private static final ParamProperties<GoParameters> WHITE_TIME_INC_PARAM = new ParamProperties<>((p,tok) -> p.time.whiteClock.incrementMs=Parser.positiveInt(tok), "winc");
//	private static final ParamProperties<GoParameters> BTIME_PARAM = new ParamProperties<>((p,tok) -> p.time.blackClock.remainingMs=Parser.positiveInt(tok), "btime");
//	private static final ParamProperties<GoParameters> BLACK_TIME_INC_PARAM = new ParamProperties<>((p,tok) -> p.time.blackClock.incrementMs=Parser.positiveInt(tok), "binc");
//	private static final ParamProperties<GoParameters> MOVES_TO_GO_PARAM = new ParamProperties<>((p,tok) -> p.time.movesToGo=Parser.positiveInt(tok), "movestogo");
//	private static final ParamProperties<GoParameters> MOVE_TIME_PARAM = new ParamProperties<>((p,tok) -> p.time.moveTimeMs=Parser.positiveInt(tok), "movetime");
//	private static final ParamProperties<GoParameters> INFINITE_PARAM = new ParamProperties<>((p,tok) -> p.time.infinite=true, "infinite");
//
//	private static final ParamProperties<GoParameters> DEPTH_PARAM = new ParamProperties<>((p,tok) -> p.depth=Parser.positiveInt(tok), "depth");
//	private static final ParamProperties<GoParameters> NODES_PARAM = new ParamProperties<>((p,tok) -> p.nodes=Parser.positiveInt(tok), "nodes");
//	private static final ParamProperties<GoParameters> MATE_PARAM = new ParamProperties<>((p,tok) -> p.mate=Parser.positiveInt(tok), "mate");
//	private static final ParamProperties<GoParameters> PONDER_PARAM = new ParamProperties<>((p,tok) -> p.ponder=true, "ponder");
//	private static final ParamProperties<GoParameters> SEARCH_MOVES_PARAM = new ParamProperties<>((p,tok) -> {
//		while (!tok.isEmpty()) {
//			p.moveToSearch.add(UCIMove.from(tok.pop()));
//		}
//	}, "searchmoves");
//
//	public static final Parser<GoParameters> PARSER = new Parser<>(Arrays.asList(WTIME_PARAM, WHITE_TIME_INC_PARAM, BTIME_PARAM, BLACK_TIME_INC_PARAM,
//			MOVES_TO_GO_PARAM, MOVE_TIME_PARAM, INFINITE_PARAM, DEPTH_PARAM, NODES_PARAM, MATE_PARAM, PONDER_PARAM, SEARCH_MOVES_PARAM));

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
