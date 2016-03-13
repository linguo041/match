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
		if (jsMatrices == null || matchState == null) {
			return null;
		}
		
		Float predictPk = matchState.getCalculatePk();

		Float weight = 0f;
		// add weight according to jiao shou records  0.05 0r 0
		if (predictPk != null) {
			if (predictPk >= 0.25) {
				if (predictPk < 2 && jsMatrices.getMatchNum() >= 2 && jsMatrices.getWinRate() > 0.4) {
					weight += 0.05f;
				}
			} else {
				if (jsMatrices.getMatchNum() >= 2 && jsMatrices.getWinDrawRate() > 0.6) {
					weight += 0.05f;
				}
			}
		}

		// add weight according to match state  -0.05 ~ 0.05
		weight += getPankouWeightByState(matchState.getHostState6(), matchState.getGuestState6(),
				hostLevel, guestlevel);
		
		return predictPk + weight;
	}
	
	private float getPankouWeightByState (LatestMatchMatrices hostMatrices,
			LatestMatchMatrices guestMatrices, TeamLevel hostLevel, TeamLevel guestLevel) {
		if (hostMatrices != null && guestMatrices != null) {
			float hostGoodRate = 0;
			float guestGoodRate = 0;
			
			if (hostLevel != null) {
				if (hostLevel.getPm() >= 4) {
					hostGoodRate = hostMatrices.getWinRate();
				} else {
					hostGoodRate = hostMatrices.getWinDrawRate();
				}
			}

			if (guestLevel != null) {
				if (guestLevel.getPm() >= 4) {
					guestGoodRate = guestMatrices.getWinRate();
				} else {
					guestGoodRate = guestMatrices.getWinDrawRate();
				}
			}

			if (hostGoodRate - guestGoodRate > 0.15) {
				return 0.05f;
			} else if (guestGoodRate - hostGoodRate > 0.15) {
				return -0.05f;
			}
		}
		
		return 0;
	}

}
