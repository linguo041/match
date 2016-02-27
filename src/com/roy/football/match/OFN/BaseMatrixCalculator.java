package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.List;

import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;
import com.roy.football.match.process.Calculator;

public class BaseMatrixCalculator extends AbstractBaseDataCalculator implements CalculateResult, Calculator<OFNCalculateResult, OFNCalculateResult> {

	@Override
	public OFNCalculateResult calucate(OFNCalculateResult matchData) {
		
		ClubMatrices matrices = matchData.getClubMatrices();
		
		matchData.setHostLevel(measureTeamLevel(matrices.getHostAllMatrix()));
		matchData.setGuestLevel(measureTeamLevel(matrices.getGuestAllMatrix()));
		
		List<TeamLabel> hostLabels = measureTeamLabel(matrices.getHostAllMatrix(), MatrixType.All);
		measureTeamLabel(matrices.getHostHomeMatrix(), MatrixType.Home, hostLabels);
		matchData.setHostLabels(hostLabels);
		
		List<TeamLabel> guestLabels = measureTeamLabel(matrices.getGuestAllMatrix(), MatrixType.All);
//		measureTeamLabel(matrices.getGuestHomeMatrix(), MatrixType.Home, guestLabels);
		matchData.setHostLabels(guestLabels);

		return matchData;
	}
	
	private TeamLevel measureTeamLevel (ClubMatrix matrix) {
		if (matrix == null) {
			return null;
		}

		for (TeamLevel level : TeamLevel.values()) {
			boolean result = matrix.getWinRt() >= level.getWinRateStd()
					&& matrix.getWinGoals() >= matrix.getWinLoseDiff() * level.getNetGoalStd()
					&& matrix.getPm() <= level.getPm()
					&& matrix.getWinDrawRt() >= level.getWinDrawRateStd()
					&& matrix.getGoals() >= matrix.getNum() * level.getGoalStd()
					&& matrix.getLoses() <= matrix.getNum() * level.getLoseStd();
					
			if (result) {
				return level;
			}
		}
		return null;
	}
	
	private List<TeamLabel> measureTeamLabel (ClubMatrix matrix, MatrixType type) {
		List<TeamLabel> labels = new ArrayList<TeamLabel>();
		
		if (matrix == null) {
			return null;
		}

		for (TeamLabel label : TeamLabel.values()) {
			boolean result = matrix.getWinRt() >= label.getWinRateStd()
					&& matrix.getWinGoals() >= matrix.getWinLoseDiff() * label.getNetGoalStd()
					&& matrix.getPm() >= label.getPm()
					&& matrix.getWinDrawRt() >= label.getWinDrawRateStd()
					&& matrix.getGoals() >= matrix.getNum() * label.getGoalStd()
					&& matrix.getLoses() <= matrix.getNum() * label.getLoseStd()
					&& type == label.getType();
					
			if (result) {
				labels.add(label);
			}
		}
		return labels;
	}
	
	private void measureTeamLabel (ClubMatrix matrix, MatrixType type, List<TeamLabel> labels) {
	
		if (matrix == null || labels == null) {
			return;
		}

		for (TeamLabel label : TeamLabel.values()) {
			boolean result = matrix.getWinRt() >= label.getWinRateStd()
					&& matrix.getWinGoals() >= matrix.getWinLoseDiff() * label.getNetGoalStd()
					&& matrix.getPm() >= label.getPm()
					&& matrix.getWinDrawRt() >= label.getWinDrawRateStd()
					&& matrix.getGoals() >= matrix.getNum() * label.getGoalStd()
					&& matrix.getLoses() <= matrix.getNum() * label.getLoseStd()
					&& type == label.getType();
					
			if (result) {
				labels.add(label);
			}
		}
	}

}
