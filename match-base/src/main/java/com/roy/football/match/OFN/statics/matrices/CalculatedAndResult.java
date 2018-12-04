package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.MatchResult;

import lombok.Data;

@Data
public class CalculatedAndResult extends OFNCalculateResult{
	private MatchResult matchResult;
}
