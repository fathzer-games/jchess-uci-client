package com.fathzer.uci.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.function.Predicate;

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
		final String best = index<0 ? reply : reply.substring(0, index);
		final Optional<String> ponder = index<0 ? Optional.empty() : Optional.of(reply.substring(index+ponderSep.length()));
		if (moves.isEmpty()) {
			return new GoReply(Collections.singletonList(new UCIMove(best)), ponder);
		}
		if (moves.keySet().stream().mapToInt(k->k).min().getAsInt()!=0 || moves.keySet().stream().mapToInt(k->k).max().getAsInt()!=moves.size()-1) {
			throw new IllegalArgumentException("There's a problem in MultiPv indexes: "+moves.keySet());
		}
		final UCIMove uciBest = moves.get(0);
		if (uciBest.getMove()==null) {
			uciBest.setMove(best);
		}
		if (!uciBest.getMove().equals(best)) {
			throw new IllegalArgumentException("Best MultiPv move ("+uciBest.getMove()+") is not best Move ()"+best);
		}
		return new GoReply(moves.values().stream().filter(m->m.getMove()!=null).toList(), ponder); 
	}
}
