package com.roy.football.match.OFN.statics.matrices;

import java.util.List;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ClubMatrices implements CalculateResult, MatchData{

	private ClubMatrix hostAllMatrix;
	private ClubMatrix hostHomeMatrix;
	private ClubMatrix hostAwayMatrix;
	private ClubMatrix guestAllMatrix;
	private ClubMatrix guestHomeMatrix;
	private ClubMatrix guestAwayMatrix;
	private TeamLevel hostLevel;
	private List<TeamLabel> hostLabels;
	private TeamLevel guestLevel;
	private List<TeamLabel> guestLabels;
	private Float hostAttGuestDefInx;
	private Float guestAttHostDefInx;
	
	@Data
	@ToString
	public static class ClubMatrix {
		private Integer num;            // total match number
		private Float winRt;            // win / num
		private Float winDrawRt;        // (win + draw) / num
		private Float drawLoseRt;       // (draw + lose) / num
		private Integer goals;          // total goaled goals
		private Integer misses;         // total missed goals
		private Integer winGoals;       // win goals = goal - miss
		private Integer winLoseDiff;    // |win - lose|
		private Integer pm;             // pai ming
		private Integer point;          // scores(win-3, draw-1, lose-0)
	}
}
