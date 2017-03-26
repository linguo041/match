package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MatchState implements CalculateResult, MatchData {

	private LatestMatchMatrices hostState6;
	private LatestMatchMatrices guestState6;
	private LatestMatchMatrices hostHome5;
	private LatestMatchMatrices guestAway5;
//	private LatestMatchMatrices hostState10;
//	private LatestMatchMatrices guestState10;
	private Float hostAttackToGuest;
	private Float guestAttackToHost;
	private Float hostAttackVariationToGuest;
	private Float guestAttackVariationToHost;
	private Float calculatePk;
	private Float hotPoint;
	
	@Data
	@ToString
	public static class LatestMatchMatrices {

		private Float winRate;
		private Float winDrawRate;
		private Float winPkRate;
		private Float winDrawPkRate;
		private Float matchGoal;
		private Float matchMiss;
		private Float point;
		private Float gVariation;                // goal
		private Float mVariation;                // miss
	}
}
