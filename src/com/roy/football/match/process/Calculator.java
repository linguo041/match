package com.roy.football.match.process;

import com.roy.football.match.base.MatchData;

public interface Calculator <T extends CalculateResult, E extends MatchData> {
	public T calucate(E matchData);
}
