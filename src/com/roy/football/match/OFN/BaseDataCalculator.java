package com.roy.football.match.OFN;

import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.ClubDatas.ClubData;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.process.Calculator;

public class BaseDataCalculator extends AbstractBaseDataCalculator implements Calculator <ClubMatrices, OFNMatchData> {

	@Override
	public ClubMatrices calucate(OFNMatchData matchData) {
		ClubDatas baseData = matchData.getBaseData();
		
		if (baseData != null) {
			ClubMatrices clubBaseMatx = new ClubMatrices();
			
			clubBaseMatx.setHostAllMatrix(calculate(baseData.getHostData(), MatrixType.All));
			clubBaseMatx.setHostHomeMatrix(calculate(baseData.getHostData(), MatrixType.Home));
			clubBaseMatx.setHostAwayMatrix(calculate(baseData.getHostData(), MatrixType.Away));
			clubBaseMatx.setGuestAllMatrix(calculate(baseData.getGuestData(), MatrixType.All));
			clubBaseMatx.setGuestHomeMatrix(calculate(baseData.getGuestData(), MatrixType.Home));
			clubBaseMatx.setGuestAwayMatrix(calculate(baseData.getGuestData(), MatrixType.Away));
			
			return clubBaseMatx;
		}
		
		return null;
	}
	
	
	private ClubMatrix calculate (ClubData clubData, MatrixType type) {
		if (clubData == null) {
			return null;
		}
		
		ClubMatrix matrix = new ClubMatrix();
		if (type == MatrixType.All) {
			matrix.setNum(clubData.getAllNum());
			matrix.setWinRt((float)clubData.getAllWin() / clubData.getAllNum());
			matrix.setWinDrawRt((float)(clubData.getAllWin() + clubData.getAllDraw()) / clubData.getAllNum());
			matrix.setWinGoals(clubData.getAllNet());
			matrix.setDrawLoseRt((float)(clubData.getAllDraw() + clubData.getAllLose()) / clubData.getAllNum());
			matrix.setWinLoseDiff(clubData.getAllWin() - clubData.getAllLose());
			matrix.setGoals(clubData.getAllGoal());
			matrix.setLoses(clubData.getAllLose());
			matrix.setPm(clubData.getPm().getAllPm());
		} else if (type == MatrixType.Home) {
			matrix.setNum(clubData.getHomeNum());
			matrix.setWinRt((float)clubData.getHomeWin() / clubData.getHomeNum());
			matrix.setWinDrawRt((float)(clubData.getHomeWin() + clubData.getHomeDraw()) / clubData.getHomeNum());
			matrix.setWinGoals(clubData.getHomeNet());
			matrix.setDrawLoseRt((float)(clubData.getHomeDraw() + clubData.getHomeLose()) / clubData.getHomeNum());
			matrix.setWinLoseDiff(clubData.getHomeWin() - clubData.getHomeLose());
			matrix.setGoals(clubData.getHomeGoal());
			matrix.setLoses(clubData.getHomeLose());
			matrix.setPm(clubData.getPm().getHomePm());
		} else if (type == MatrixType.Away) {
			matrix.setNum(clubData.getAwayNum());
			matrix.setWinRt((float)clubData.getAwayWin() / clubData.getAwayNum());
			matrix.setWinDrawRt((float)(clubData.getAwayWin() + clubData.getAwayDraw()) / clubData.getAwayNum());
			matrix.setWinGoals(clubData.getAwayNet());
			matrix.setDrawLoseRt((float)(clubData.getAwayDraw() + clubData.getAwayLose()) / clubData.getAwayNum());
			matrix.setWinLoseDiff(clubData.getAwayWin() - clubData.getAwayLose());
			matrix.setGoals(clubData.getAwayGoal());
			matrix.setLoses(clubData.getAwayLose());
			matrix.setPm(clubData.getPm().getAwayPm());
		}

		return matrix;
	}
}
