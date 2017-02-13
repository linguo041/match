package com.roy.football.match.OFN;

import org.springframework.stereotype.Component;

import com.roy.football.match.Exception.CommonException;
import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.ClubDatas.ClubData;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.service.HistoryMatchCalculationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BaseDataCalculator extends AbstractBaseDataCalculator implements Calculator <ClubMatrices, OFNMatchData> {

	@Override
	public ClubMatrices calucate(OFNMatchData matchData) {
		ClubDatas baseData = matchData.getBaseData();

		if (baseData != null) {
			try {
				ClubMatrices clubBaseMatx = new ClubMatrices();
				
				clubBaseMatx.setHostAllMatrix(calculate(baseData.getHostData(), MatrixType.All));
				clubBaseMatx.setHostHomeMatrix(calculate(baseData.getHostData(), MatrixType.Home));
				clubBaseMatx.setHostAwayMatrix(calculate(baseData.getHostData(), MatrixType.Away));
				clubBaseMatx.setGuestAllMatrix(calculate(baseData.getGuestData(), MatrixType.All));
				clubBaseMatx.setGuestHomeMatrix(calculate(baseData.getGuestData(), MatrixType.Home));
				clubBaseMatx.setGuestAwayMatrix(calculate(baseData.getGuestData(), MatrixType.Away));

				return clubBaseMatx;
			} catch (CommonException e) {
				log.error(String.format("Unable to calculate the base data for match %s", matchData.getMatchId()), e);
			}
		}
		
		return null;
	}
	
	@Override
	public void calucate(ClubMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private ClubMatrix calculate (ClubData clubData, MatrixType type) throws CommonException {
		if (clubData == null) {
			throw new CommonException("Club base data is empty.");
		}
		
		ClubMatrix matrix = new ClubMatrix();
		if (type == MatrixType.All) {
			matrix.setNum(clubData.getAllNum());
			matrix.setWinRt((float)clubData.getAllWin() / clubData.getAllNum());
			matrix.setWinDrawRt((float)(clubData.getAllWin() + clubData.getAllDraw()) / clubData.getAllNum());
			matrix.setWinGoals(clubData.getAllNet());
			matrix.setDrawLoseRt((float)(clubData.getAllDraw() + clubData.getAllLose()) / clubData.getAllNum());
			matrix.setWinLoseDiff(Math.abs(clubData.getAllWin() - clubData.getAllLose()));
			matrix.setGoals(clubData.getAllGoal());
			matrix.setMisses(clubData.getAllMiss());
			matrix.setPm(clubData.getPm().getAllPm());
			matrix.setPoint(clubData.getAllScore());
		} else if (type == MatrixType.Home) {
			matrix.setNum(clubData.getHomeNum());
			matrix.setWinRt((float)clubData.getHomeWin() / clubData.getHomeNum());
			matrix.setWinDrawRt((float)(clubData.getHomeWin() + clubData.getHomeDraw()) / clubData.getHomeNum());
			matrix.setWinGoals(clubData.getHomeNet());
			matrix.setDrawLoseRt((float)(clubData.getHomeDraw() + clubData.getHomeLose()) / clubData.getHomeNum());
			matrix.setWinLoseDiff(Math.abs(clubData.getHomeWin() - clubData.getHomeLose()));
			matrix.setGoals(clubData.getHomeGoal());
			matrix.setMisses(clubData.getHomeMiss());
			matrix.setPm(clubData.getPm().getHomePm());
			matrix.setPoint(clubData.getHomeScore());
		} else if (type == MatrixType.Away) {
			matrix.setNum(clubData.getAwayNum());
			matrix.setWinRt((float)clubData.getAwayWin() / clubData.getAwayNum());
			matrix.setWinDrawRt((float)(clubData.getAwayWin() + clubData.getAwayDraw()) / clubData.getAwayNum());
			matrix.setWinGoals(clubData.getAwayNet());
			matrix.setDrawLoseRt((float)(clubData.getAwayDraw() + clubData.getAwayLose()) / clubData.getAwayNum());
			matrix.setWinLoseDiff(Math.abs(clubData.getAwayWin() - clubData.getAwayLose()));
			matrix.setGoals(clubData.getAwayGoal());
			matrix.setMisses(clubData.getAwayMiss());
			matrix.setPm(clubData.getPm().getAwayPm());
			matrix.setPoint(clubData.getAwayScore());
		}

		return matrix;
	}
}
