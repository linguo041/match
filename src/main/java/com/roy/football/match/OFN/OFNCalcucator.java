package com.roy.football.match.OFN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.base.League;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.okooo.OkoooMatchCrawler;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.service.HistoryMatchCalculationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OFNCalcucator implements Calculator<OFNCalculateResult, OFNMatchData> {
	@Autowired
	private BaseDataCalculator baseCalculator;
	
	@Autowired
	private BaseMatrixCalculator baseMatrixCalculator;
	
	@Autowired
	private JiaoShouCalculator jiaoshouCalculator;
	
	@Autowired
	private LatestMatchCalculator latestMatchCalculator;
	
	@Autowired
	private PankouCalculator pankouCalculator;
	@Autowired
	private DaxiaoCalculator dxCalculator;
	@Autowired
	private EuroCalculator euroCalculator;
	@Autowired
	private OkoooMatchCrawler okoooMatchCrawler;
	
	@Autowired
	private PankouKillPromoter pankouKiller;
	
	// more....

	@Override
	public OFNCalculateResult calucate(OFNMatchData matchData) {
		
		if (matchData == null) {
			return null;
		}
		OFNCalculateResult calResult= new OFNCalculateResult();
		
		TeamLevel hostLevel = null;
		TeamLevel guestLevel = null;

		ClubMatrices matrices = baseCalculator.calucate(matchData);
		if (matrices != null) {
			baseMatrixCalculator.calucate(matrices);
			calResult.setClubMatrices(matrices);
			
			hostLevel = matrices.getHostLevel();
			guestLevel = matrices.getGuestLevel();
			matchData.setLevelDiff(hostLevel.ordinal() - guestLevel.ordinal());
		}

		calResult.setLeague(matchData.getLeague());
		calResult.setSameCityOrNeutral(matchData.isSameCityOrNeutral());

		JiaoShouMatrices jsMatrices = jiaoshouCalculator.calucate(matchData);
		calResult.setJiaoShou(jsMatrices);
		
		PankouMatrices pkMatrices = pankouCalculator.calucate(matchData, Company.Aomen);
		calResult.setPkMatrices(pkMatrices);
		if (pkMatrices != null) {
			matchData.setOriginalPk(pkMatrices.getOriginPk().getPanKou());
			matchData.setMainPk(pkMatrices.getMainPk().getPanKou());
		}
		
		MatchState matchState = latestMatchCalculator.calucate(matchData);
		calResult.setMatchState(matchState);
		
		PankouMatrices ysbPkMatrices = pankouCalculator.calucate(matchData, Company.YiShenBo);
		calResult.setYsbPkMatrices(ysbPkMatrices);
		
		DaxiaoMatrices dxMatrices = dxCalculator.calucate(matchData);
		calResult.setDxMatrices(dxMatrices);
		
		Float predictPanKou = getPredictPanKou(jsMatrices, matchState,
				matrices == null ? TeamLevel.Nomal : matrices.getHostLevel(),
				matrices == null ? TeamLevel.Nomal : matrices.getGuestLevel());
		calResult.setPredictPanKou(predictPanKou);

		EuroMatrices euroMatrices = euroCalculator.calucate(matchData);

		calResult.setEuroMatrices(euroMatrices);
		
		Long okMatchId = matchData.getOkoooMatchId();
		if (okMatchId != null) {
			MatchExchangeData exchangeData =okoooMatchCrawler.getExchangeData(okMatchId);
			calResult.setExchanges(exchangeData);
		}
		
		predict(calResult);

		return calResult;
	}
	
	public void predict (OFNCalculateResult calResult) {
		if (calResult != null) {
			try {
				PredictResult predictRes = pankouKiller.calculate(calResult);
				calResult.setPredictResult(predictRes);
			} catch (Exception e) {
				log.error("Unable to predict match result.", e);
			}
		}
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
			 
			// calculate win weight, need refer to the team's level, team is stronger, easier to win 
			if (hostLevel != null) {
				hostWeight = hostMatrices.getWinRate() * (0.135 + hostLevel.ordinal() * 0.035 / 3)
						+ (hostMatrices.getWinDrawRate() - hostMatrices.getWinRate()) * (0.06 + hostLevel.ordinal() * 0.035 / 3) ;
			}

			if (guestLevel != null) {
				guestWeight = guestMatrices.getWinRate() * (0.135 + guestLevel.ordinal() * 0.035 / 3)
						+ (guestMatrices.getWinDrawRate()-guestMatrices.getWinRate()) * (0.06 + guestLevel.ordinal() * 0.035 / 3);
			}
			
			// win weight: -0.15 ~ 0.15
			if (hostWeight != null && guestWeight != null) {
				pkWeight = (float)(hostWeight - guestWeight);
			}

			// pk weight: -0.15 ~ 0.15
			float winPkRateDiff = (hostMatrices.getWinPkRate() - guestMatrices.getWinPkRate()) * 0.075f;
			float wdPkRateDiff = (hostMatrices.getWinDrawPkRate() - guestMatrices.getWinDrawPkRate()) * 0.075f;
			
			pkWeight += (winPkRateDiff + wdPkRateDiff);
		}
		
		return pkWeight;
	}
}
