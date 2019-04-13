package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchContinent;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.Calculator;

@Component
public class BaseMatrixCalculator extends AbstractBaseDataCalculator implements Calculator<ClubMatrices, ClubMatrices> {

	@Override
	public ClubMatrices calucate(ClubMatrices matrices) {

		matrices.setHostLevel(measureTeamLevel(matrices.getHostAllMatrix()));
		matrices.setGuestLevel(measureTeamLevel(matrices.getGuestAllMatrix()));

		List<TeamLabel> hostLabels = measureTeamPositiveLabel(matrices.getHostAllMatrix(), MatrixType.All);
		measureTeamPositiveLabel(matrices.getHostHomeMatrix(), MatrixType.Home, hostLabels);
		matrices.setHostLabels(hostLabels);

		List<TeamLabel> guestLabels = measureTeamPositiveLabel(matrices.getGuestAllMatrix(), MatrixType.All);
		measureTeamNegativeLabel(matrices.getGuestAwayMatrix(), MatrixType.Away, guestLabels);
		matrices.setGuestLabels(guestLabels);

		compareClubsAttackDefend(matrices);

		return matrices;
	}
	
	@Override
	public void calucate(ClubMatrices matchResult, ClubMatrices matchData) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean calDistinctHomeAway (ClubMatrices matrices, League le) {
		/*
		 * check one status: host home is strong, guest away is week
		 * 		away is strong always means all is strong
		 */
		
		if (matrices == null) {
			return false;
		}
		
		ClubMatrix hostAllMatrix = matrices.getHostAllMatrix();
		ClubMatrix guestAllMatrix = matrices.getGuestAllMatrix();
		ClubMatrix hostHomeMatrix = matrices.getHostHomeMatrix();
		ClubMatrix guestAwayMatrix = matrices.getGuestAwayMatrix();
		
		int pmThreshold = le.getClubNum() > 20 ? 4
				: (le.getClubNum() >= 16 ? 3 : 2);
		
		if (le.getContinent() == MatchContinent.America) {
			pmThreshold = 2;
		}

		if (hostHomeMatrix != null && guestAwayMatrix != null && hostHomeMatrix.getNum() > 3 && guestAwayMatrix.getNum() > 3) {
			int hostHomeAll = -1 * (hostHomeMatrix.getPm() - hostAllMatrix.getPm());
			int guestAwayAll = -1 * (guestAwayMatrix.getPm() - guestAllMatrix.getPm());
			
			float hostWinToAll = hostHomeMatrix.getWinRt() - hostAllMatrix.getWinRt();
			float guestLostToAll = guestAllMatrix.getWinDrawRt() - guestAwayMatrix.getWinDrawRt();
			
			if (hostHomeAll >= pmThreshold || guestAwayAll <= -1 * pmThreshold) {
				return true;
			}
			
			if (hostWinToAll >= 0.3f || guestLostToAll >= 0.3f) {
				return true;
			}
		}
		
		return false;
	}
	
	private void compareClubsAttackDefend (ClubMatrices matrices) {
		if (matrices == null) {
			return;
		}
		
		ClubMatrix hostClub = matrices.getHostHomeMatrix();
		ClubMatrix guestClub = matrices.getGuestAwayMatrix();

		if (hostClub != null && hostClub.getNum() < 5) {
			hostClub = matrices.getHostAllMatrix();
		}
		
		if (guestClub != null && guestClub.getNum() < 5) {
			guestClub = matrices.getGuestAllMatrix();
		}

		if (hostClub != null && guestClub != null) {
			// goal is main key here
			matrices.setHostAttGuestDefInx(0.6f * hostClub.getGoals() / hostClub.getNum() + 0.4f * guestClub.getMisses() / guestClub.getNum());
			matrices.setGuestAttHostDefInx(0.6f * guestClub.getGoals() / guestClub.getNum() + 0.4f * hostClub.getMisses() / hostClub.getNum());
			matrices.setHostWinRt(hostClub.getWinRt());
			matrices.setGuestWinRt(guestClub.getWinRt());
		}
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
	
		if (matrix == null || CollectionUtils.isEmpty(labels)) {
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
		
		if (matrix == null || CollectionUtils.isEmpty(labels)) {
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
