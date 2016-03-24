package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.List;

import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.Calculator;

public class BaseMatrixCalculator extends AbstractBaseDataCalculator implements Calculator<OFNCalculateResult, ClubMatrices> {

	@Override
	public OFNCalculateResult calucate(ClubMatrices matrices) {
		OFNCalculateResult calResult = new OFNCalculateResult();
		
		calResult.setClubMatrices(matrices);

		calResult.setHostLevel(measureTeamLevel(matrices.getHostAllMatrix()));
		calResult.setGuestLevel(measureTeamLevel(matrices.getGuestAllMatrix()));
		
		List<TeamLabel> hostLabels = measureTeamPositiveLabel(matrices.getHostAllMatrix(), MatrixType.All);
		measureTeamPositiveLabel(matrices.getHostHomeMatrix(), MatrixType.Home, hostLabels);
		calResult.setHostLabels(hostLabels);
		
		List<TeamLabel> guestLabels = measureTeamPositiveLabel(matrices.getGuestAllMatrix(), MatrixType.All);
		measureTeamNegativeLabel(matrices.getGuestAwayMatrix(), MatrixType.Away, guestLabels);
		calResult.setGuestLabels(guestLabels);

		return calResult;
	}
	
	@Override
	public void calucate(OFNCalculateResult Result, ClubMatrices matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private TeamLevel measureTeamLevel (ClubMatrix matrix) {
		if (matrix == null) {
			return null;
		}

		for (TeamLevel level : TeamLevel.values()) {
			boolean result = matrix.getWinRt() >= level.getWinRateStd()
//					&& matrix.getWinGoals() >= (int)((matrix.getWinLoseDiff()==0 ? 0.1 : matrix.getWinLoseDiff()) * level.getNetGoalStd())
					&& matrix.getPm() <= level.getPm()
					&& matrix.getWinDrawRt() >= level.getWinDrawRateStd()
					&& matrix.getGoals() >= (int)(matrix.getNum() * level.getGoalStd())
					&& matrix.getMisses() <= (int)(matrix.getNum() * level.getMissStd());
					
			if (result) {
				return level;
			}
		}
		return null;
	}
	
	private List<TeamLabel> measureTeamPositiveLabel (ClubMatrix matrix, MatrixType type) {
		List<TeamLabel> labels = new ArrayList<TeamLabel>();
		
		if (matrix == null) {
			return null;
		}

		for (TeamLabel label : TeamLabel.values()) {
			boolean result = matrix.getWinRt() >= label.getWinRateStd()
					&& matrix.getWinGoals() >= matrix.getWinLoseDiff() * label.getNetGoalStd()
					&& matrix.getPm() <= label.getPm()
					&& matrix.getWinDrawRt() >= label.getWinDrawRateStd()
					&& matrix.getGoals() >= matrix.getNum() * label.getGoalStd()
					&& matrix.getMisses() <= matrix.getNum() * label.getMissStd()
					&& type == label.getType();
					
			if (result) {
				labels.add(label);
			}
		}
		return labels;
	}
	
	private void measureTeamPositiveLabel (ClubMatrix matrix, MatrixType type, List<TeamLabel> labels) {
	
		if (matrix == null || labels == null) {
			return;
		}

		for (TeamLabel label : TeamLabel.values()) {
			boolean result = matrix.getWinRt() >= label.getWinRateStd()
					&& matrix.getWinGoals() >= matrix.getWinLoseDiff() * label.getNetGoalStd()
					&& matrix.getPm() <= label.getPm()
					&& matrix.getWinDrawRt() >= label.getWinDrawRateStd()
					&& matrix.getGoals() >= matrix.getNum() * label.getGoalStd()
					&& matrix.getMisses() <= matrix.getNum() * label.getMissStd()
					&& type == label.getType();
					
			if (result) {
				labels.add(label);
			}
		}
	}
	
	private void measureTeamNegativeLabel (ClubMatrix matrix, MatrixType type, List<TeamLabel> labels) {
		
		if (matrix == null || labels == null) {
			return;
		}

		for (TeamLabel label : TeamLabel.values()) {
			boolean result = matrix.getWinRt() <= label.getWinRateStd()
					&& matrix.getWinGoals() <= matrix.getWinLoseDiff() * label.getNetGoalStd()
					&& matrix.getPm() >= label.getPm()
					&& matrix.getWinDrawRt() <= label.getWinDrawRateStd()
					&& matrix.getGoals() <= matrix.getNum() * label.getGoalStd()
					&& matrix.getMisses() >= matrix.getNum() * label.getMissStd()
					&& type == label.getType();
					
			if (result) {
				labels.add(label);
			}
		}
	}
}
