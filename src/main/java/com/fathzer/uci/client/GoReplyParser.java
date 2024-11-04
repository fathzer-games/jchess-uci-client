package com.fathzer.uci.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fathzer.uci.client.GoReply.Score;
import com.fathzer.uci.client.GoReply.UCIMove;

class GoReplyParser {
	private static final String BEST_MOVE_PREFIX = "bestmove ";
	
	static final Predicate<String> IS_REPLY =  s -> s.startsWith(BEST_MOVE_PREFIX);
	
	private Map<Integer, UCIMove> moves = new TreeMap<>();
	
	void parseInfo(String line) {
		final String infoPrefix = "info ";
		if (!line.startsWith(infoPrefix)) {
			return;
		}
		line = line.substring(infoPrefix.length()-1);
		final int moveIndex = getMoveIndex(line);
		final UCIMove move = moves.computeIfAbsent(moveIndex, k -> new UCIMove(null));
		final Optional<Score> score = getScore(line);
		score.ifPresent(s -> move.setScore(score));
		final Optional<List<String>> pv = getPv(line);
		pv.ifPresent(p -> {
			move.setPv(pv);
			move.setMove(p.get(0));
		});
		final OptionalInt depth = getInt(line, "depth");
		depth.ifPresent(d-> move.setDepth(depth));
		final OptionalInt selDepth = getInt(line, "seldepth");
		selDepth.ifPresent(s-> move.setSelDepth(selDepth));
	}
	
	private int getMoveIndex(String line) {
		final OptionalInt index = getInt(line, "multipv");
		return index.isEmpty() ? 0 : index.getAsInt()-1;
	}
	
	private OptionalInt getInt(String line, String keyword) {
		final int index = line.indexOf(" "+keyword+" ");
		return index<0 ? OptionalInt.empty() : OptionalInt.of(Integer.parseInt(line.substring(index+1).split(" ")[1]));
	}

	private Optional<Score> getScore(String line) {
		final int index = line.indexOf("score");
		if (index<0) {
			return Optional.empty();
		}
		final var tokens = line.substring(index).split(" ");
		final var type = tokens[1];
		final var value = Integer.parseInt(tokens[2]);
		if ("cp".equals(type)) {
			return Optional.of(new GoReply.CpScore(value));
		} else if ("mate".equals(type)) {
			return Optional.of(new GoReply.MateScore(value));
		} else if ("lowerbound".equals(type)) {
			return Optional.of(new GoReply.LowerScore(value));
		} else if ("upperbound".equals(type)) {
			return Optional.of(new GoReply.UpperScore(value));
		} else {
			throw new IllegalArgumentException("Unknown score type "+type);
		}
	}
	
	private Optional<List<String>> getPv(String line) {
		final int index = line.indexOf(" pv ");
		if (index<0) {
			return Optional.empty();
		}
		return Optional.of(Arrays.asList(line.substring(index+4).split(" ")));
	}


	GoReply get(String reply) {
		reply = reply.substring(BEST_MOVE_PREFIX.length());
		if ("(none)".equalsIgnoreCase(reply)) {
			return new GoReply(Collections.emptyList(), Optional.empty());
		}
		final String ponderSep = " ponder ";
		final int index = reply.indexOf(ponderSep);
		final String playedMove = index<0 ? reply : reply.substring(0, index);
		final Optional<String> ponder = index<0 ? Optional.empty() : Optional.of(reply.substring(index+ponderSep.length()));
		if (moves.isEmpty()) {
			// There is no info lines => return the move
			return new GoReply(Collections.singletonList(new UCIMove(playedMove)), ponder);
		}
		if (moves.keySet().stream().mapToInt(k->k).min().getAsInt()!=0 || moves.keySet().stream().mapToInt(k->k).max().getAsInt()!=moves.size()-1) {
			throw new IllegalArgumentException("There's a problem in MultiPv indexes: "+moves.keySet());
		}
		// WARNING with some engines like Stockfish at level < 20, the played move returned by go is not always the first one and
		// it can not be present in the info lines!
		// So we should be cautious. First extract the played move from the info lines stream, then build the returned moves.
		// As far as I understand the uci protocol, if there's no multiPV, the pv should concern the played move if no move is specified in it. 
		if (moves.size()==1 && moves.get(0).getMove()==null) {
			final UCIMove uciPlayed = moves.get(0);
			uciPlayed.setMove(playedMove);
			return new GoReply(Collections.singletonList(uciPlayed), ponder);
		}
		final Optional<UCIMove> uciPlayedOpt = moves.values().stream().filter(m -> playedMove.equals(m.getMove())).findAny();
		final UCIMove uciPlayed = uciPlayedOpt.orElse(new UCIMove(playedMove));
		final Stream<UCIMove> others = moves.values().stream().filter(m->m.getMove()!=null&&!uciPlayed.getMove().equals(m.getMove()));
		return new GoReply(Stream.concat(Stream.of(uciPlayed), others).toList(), ponder); 
	}
}
