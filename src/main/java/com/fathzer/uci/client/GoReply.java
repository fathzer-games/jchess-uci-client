package com.fathzer.uci.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** The reply to a go request.
 */
public class GoReply {
	/** A score.*/
	public sealed interface Score {
		/** Gets the UCI representation of a score.
		 * @return a String
		 */
		String toUCI();
	}
	/** An exact score expressed in centipawns.
	 * @param cp The number of centipawns
	 */
	public final record CpScore (int cp) implements Score {
		@Override
		public String toUCI() {
			return "cp "+cp;
		}
	}
	/** A lower bound score expressed in centipawns.
	 * @param cp The number of centipawns
	 */
	public final record LowerScore (int cp) implements Score {
		@Override
		public String toUCI() {
			return "lowerbound "+cp;
		}
	}
	/** An upper bound score expressed in centipawns.
	 * @param cp The number of centipawns
	 */
	public final record UpperScore (int cp) implements Score {
		@Override
		public String toUCI() {
			return "upperbound "+cp;
		}
	}
	/** A mate score.
	 * @param moveNumber The number of moves (not plies) before mate. A negative number if engine is mated.
	 */
	public final record MateScore (int moveNumber) implements Score {
		@Override
		public String toUCI() {
			return "mate "+moveNumber;
		}
	}

	/** The information attached to the reply (the information returned in info lines).
	 */
	public static class UCIMove {
		private String move;
		private OptionalInt depth;
		private OptionalInt selDepth;
		private Optional<Score> score;
		private Optional<List<String>> pv;
		
		/** Constructor.
		 * @param move The move in uci format.
		 */
		public UCIMove(String move) {
			this.move = move;
			this.score = Optional.empty();
			this.pv = Optional.empty();
		}

		public String getMove() {
			return move;
		}

		public void setMove(String move) {
			this.move = move;
		}

		public Optional<List<String>> getPv() {
			return pv;
		}

		public void setPv(Optional<List<String>> pv) {
			this.pv = pv;
		}

		public Optional<Score> getScore() {
			return score;
		}

		public void setScore(Optional<Score> score) {
			this.score = score;
		}

		public OptionalInt getDepth() {
			return depth;
		}

		public void setDepth(OptionalInt depth) {
			this.depth = depth;
		}

		public OptionalInt getSelDepth() {
			return selDepth;
		}

		public void setSelDepth(OptionalInt selDepth) {
			this.selDepth = selDepth;
		}
	}
	
	private final List<UCIMove> moves;
	private final Optional<String> ponderMove;
	
	public GoReply(UCIMove move) {
		this(new LinkedList<>(Collections.singletonList(move)), Optional.empty());
	}
	
	public GoReply(List<UCIMove> moves, Optional<String> ponderMove) {
		this.moves = moves;
		this.ponderMove = ponderMove;
	}
	
	public void addMove(UCIMove move) {
		this.moves.add(move);
	}

	public List<UCIMove> getMoves() {
		return moves;
	}
	
	public Optional<String> getPonderMove() {
		return ponderMove;
	}
}
