package com.roy.football.match.OFN;

import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.Calculator;

public class OFNCalcucator implements Calculator<OFNCalculateResult, OFNMatchData> {
	private final static BaseDataCalculator baseCalculator = new BaseDataCalculator();
	private final static BaseMatrixCalculator baseMatrixCalculator = new BaseMatrixCalculator();
	private final static JiaoShouCalculator jiaoshouCalculator = new JiaoShouCalculator();
	private final static LatestMatchCalculator latestMatchCalculator = new LatestMatchCalculator();
	private final static PankouCalculator pankouCalculator = new PankouCalculator();
	private final static EuroCalculator euroCalculator = new EuroCalculator();
	private final static PankouKillPromoter pankouKiller = new PankouKillPromoter();
	// more....

	@Override
	public OFNCalculateResult calucate(OFNMatchData matchData) {
		
		if (matchData == null) {
			return null;
		}

		ClubMatrices matrices = baseCalculator.calucate(matchData);

		OFNCalculateResult calResult = baseMatrixCalculator.calucate(matrices);
		calResult.setLeagueId(matchData.getLeagueId());
		
		JiaoShouMatrices jsMatrices = jiaoshouCalculator.calucate(matchData);
		calResult.setJiaoShou(jsMatrices);
		
		MatchState matchState = latestMatchCalculator.calucate(matchData);
		calResult.setMatchState(matchState);
		
		PankouMatrices pkMatrices = pankouCalculator.calucate(matchData);
		calResult.setPkMatrices(pkMatrices);
		
		Float predictPanKou = getPredictPanKou(jsMatrices, matchState, calResult.getHostLevel(), calResult.getGuestLevel());
		calResult.setPredictPanKou(predictPanKou);
		
		EuroMatrices euroMatrices = euroCalculator.calucate(matchData);
		calResult.setEuroMatrices(euroMatrices);
		
		OFNKillPromoteResult killPromoteRes = pankouKiller.calculate(calResult);
		calResult.setKillByPk(killPromoteRes.getKillByPk());
		calResult.setKillByPl(killPromoteRes.getKillByPl());
		calResult.setPromote(killPromoteRes.getPromoteByPk());
		calResult.setTooHot(killPromoteRes.getTooHot());

		return calResult;
	}

	@Override
	public void calucate(OFNCalculateResult Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	// TODO - refer to the team status
	private Float getPredictPanKou (JiaoShouMatrices jsMatrices, MatchState matchState, TeamLevel hostLevel, TeamLevel guestlevel) {
		Float predictPk = null;
		
		Float weight = 0f;

		if (matchState != null) {
			predictPk = matchState.getCalculatePk();
			
			// add weight according to match state  -0.05 ~ 0.05
			weight = getPankouWeightByState(matchState.getHostState6(), matchState.getGuestState6(),
					hostLevel, guestlevel);
		}
		
		if (jsMatrices != null && jsMatrices.getMatchNum() >= 2) {
			// add weight according to jiao shou records  0.05 0r 0
			if (predictPk != null) {
				if (predictPk >= 1 || predictPk <= -0.2) {
					weight += (jsMatrices.getWinPkRate() + jsMatrices.getWinDrawPkRate() -1) * 0.16f;
				} else {
					weight += (jsMatrices.getWinRate() + jsMatrices.getWinDrawRate() -1) * 0.16f;
				}
			}
		}

		if (predictPk != null) {
			return predictPk + weight;
		}
		return null;
	}
	
	private float getPankouWeightByState (LatestMatchMatrices hostMatrices,
			LatestMatchMatrices guestMatrices, TeamLevel hostLevel, TeamLevel guestLevel) {
		float pkWeight = 0;
		
		if (hostMatrices != null && guestMatrices != null) {
			Double hostWeight = null;
			Double guestWeight = null;
			 
			
			if (hostLevel != null) {
				hostWeight = hostMatrices.getWinRate() * (0.135 + hostLevel.ordinal() * 0.035 / 3)
						+ (hostMatrices.getWinDrawRate() - hostMatrices.getWinRate()) * (0.06 + hostLevel.ordinal() * 0.035 / 3) ;
			}

			if (guestLevel != null) {
				guestWeight = guestMatrices.getWinRate() * (0.135 + guestLevel.ordinal() * 0.035 / 3)
						+ (guestMatrices.getWinDrawRate()-guestMatrices.getWinRate()) * (0.06 + guestLevel.ordinal() * 0.035 / 3);
			}
			
			if (hostWeight != null && guestWeight != null) {
				pkWeight = (float)(hostWeight - guestWeight);
			}

			float winPkRateDiff = (hostMatrices.getWinPkRate() - guestMatrices.getWinPkRate()) * 0.075f;
			float wdPkRateDiff = (hostMatrices.getWinDrawPkRate() - guestMatrices.getWinDrawPkRate()) * 0.075f;
			
			pkWeight += (winPkRateDiff + wdPkRateDiff);
		}
		
		return pkWeight;
	}

}
