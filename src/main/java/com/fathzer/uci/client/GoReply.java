package com.fathzer.uci.client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
		private final String move;
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
//	
//	@Override
//	/** Gets the uci representation of the reply.
//	 * @return a String
//	 * @see #getMainInfoString()
//	 */
//	public String toString() {
//		return "bestmove "+(bestMove==null?"(none)":bestMove)+(ponderMove==null?"":(" "+ponderMove));
//	}
//	
//	/** Gets the uci info line to return just before sending the reply.
//	 * @return The line or an empty optional if no information is available
//	 */
//	public Optional<String> getMainInfoString() {
//		return bestMove==null ? Optional.empty() : getInfoString(0);
//	}
//
//	/** Gets the uci info line to return just before sending the reply.
//	 * @param index The move index (0 for the best move or the index or the extra moves passed to {@code Info#setExtraMoves(List)} +1
//	 * @return The line or an empty optional if no information is available
//	 */
//	public Optional<String> getInfoString(int index) {
//		final StringBuilder builder = new StringBuilder();
//		if (info.depth>0) {
//			builder.append("depth ").append(info.depth);
//		}
//		final UCIMove move = index==0 ? bestMove : info.extraMoves.get(index-1);
//		final Optional<Score> score = info.scoreBuilder.apply(move);
//		if (score.isPresent()) {
//			if (!builder.isEmpty()) {
//				builder.append(' ');
//			}
//			builder.append("score ").append(score.get().toUCI());
//		}
//		if (info.hashFull>0) {
//			if (!builder.isEmpty()) {
//				builder.append(' ');
//			}
//			builder.append("hashfull ").append(info.hashFull);
//		}
//		final Optional<List<UCIMove>> pv = info.pvBuilder.apply(move);
//		if (pv.isPresent()) {
//			if (!builder.isEmpty()) {
//				builder.append(' ');
//			}
//			final String moves = String.join(" ", pv.get().stream().map(UCIMove::toString).toList());
//			builder.append("multipv ").append(index+1).append(" pv ").append(moves);
//		}
//		return builder.isEmpty() ? Optional.empty() : Optional.of("info "+builder);
//	}
}
