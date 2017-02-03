package com.roy.football.match.jpa;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.LatestMatchMatrixType;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.jpa.entities.calculation.EAsiaPk;
import com.roy.football.match.jpa.entities.calculation.EDaXiaoPk;
import com.roy.football.match.jpa.entities.calculation.EEuroPlCompany;
import com.roy.football.match.jpa.entities.calculation.EEuroPlState;
import com.roy.football.match.jpa.entities.calculation.EExchange;
import com.roy.football.match.jpa.entities.calculation.EJiaoShou;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchDetail;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchState;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchClubDetail;
import com.roy.football.match.jpa.entities.calculation.EMatchClubState;
import com.roy.football.match.jpa.entities.calculation.EMatchResult;
import com.roy.football.match.jpa.entities.calculation.EMatchResultDetail;
import com.roy.football.match.jpa.entities.calculation.EPredictResult;
import com.roy.football.match.okooo.MatchExchangeData;

public class EntiryReverseConverter {

	public static OFNMatchData fromEMatch (EMatch ematch) {
		OFNMatchData m = new OFNMatchData();
		m.setMatchId(ematch.getOfnMatchId());
		m.setMatchDayId(ematch.getMatchDayId());
		m.setMatchTime(ematch.getMatchTime());
		m.setOkoooMatchId(ematch.getOkoooMatchId());
		m.setHostId(ematch.getHostId());
		m.setHostName(ematch.getHostName());
		m.setGuestId(ematch.getGuestId());
		m.setGuestName(ematch.getGuestName());
		m.setLeague(ematch.getLeague());
		return m;
	}
	
	public static PankouMatrices fromEAsiaPk (EAsiaPk epk) {
		PankouMatrices pk = new PankouMatrices();
		pk.setHwinChangeRate(epk.getHwinChangeRate());
		pk.setAwinChangeRate(epk.getAwinChangeRate());
		pk.setHours(epk.getHours());
		pk.setOriginPk(new AsiaPl(epk.getOriginHWin(), epk.getOriginAWin(), epk.getOriginPk()));
		pk.setCurrentPk(new AsiaPl(epk.getCurrentHWin(), epk.getCurrentAWin(), epk.getCurrentPk()));
		pk.setMainPk(new AsiaPl(epk.getMainHWin(), epk.getMainAWin(), epk.getMainPk()));
		return pk;
	}
	
	public static DaxiaoMatrices fromEDaXiaoPk (EDaXiaoPk epk) {
		DaxiaoMatrices pk = new DaxiaoMatrices();
		pk.setDaChangeRate(epk.getDaChangeRate());
		pk.setXiaoChangeRate(epk.getXiaoChangeRate());
		pk.setHours(epk.getHours());
		pk.setOriginPk(new AsiaPl(epk.getOriginHWin(), epk.getOriginAWin(), epk.getOriginPk()));
		pk.setCurrentPk(new AsiaPl(epk.getCurrentHWin(), epk.getCurrentAWin(), epk.getCurrentPk()));
		pk.setMainPk(new AsiaPl(epk.getMainHWin(), epk.getMainAWin(), epk.getMainPk()));
		return pk;
	}
	
	public static EuroMatrices fromEEuroPlState (EEuroPlState pl) {
		EuroMatrices ems = new EuroMatrices();
		ems.setMainAvgWinDiff(pl.getMainAvgWinDiff());
		ems.setMainAvgDrawDiff(pl.getMainAvgDrawDiff());
		ems.setMainAvgLoseDiff(pl.getMainAvgLoseDiff());
		
		if (pl.getCompanyPls() != null && pl.getCompanyPls().size() > 0) {
			Map<Company, EuroMatrix> companyEus = Maps.newHashMap();
			
			for (EEuroPlCompany epl : pl.getCompanyPls()) {
				companyEus.put(epl.getCompany(), fromEEuroPlCompany(epl));
			}
			
			ems.setCompanyEus(companyEus);
		}
		
		ems.setCurrEuroAvg(new EuroPl(pl.getAvgWin(), pl.getAvgDraw(), pl.getAvgLose(), null));
		
		return ems;
	}
	
	public static EuroMatrix fromEEuroPlCompany (EEuroPlCompany epl) {
		EuroMatrix em = new EuroMatrix();
		EuroPl current = new EuroPl(epl.getCurrentEWin(), epl.getCurrentEDraw(), epl.getCurrentELose(), null);
		EuroPl origin = new EuroPl(epl.getOriginEWin(), epl.getOriginEDraw(), epl.getOriginELose(), null);
		EuroPl main = new EuroPl(epl.getMainEWin(), epl.getMainEDraw(), epl.getMainELose(), null);
		em.setCurrentEuro(current);
		em.setOriginEuro(origin);
		em.setMainEuro(main);
		em.setWinChange(epl.getWinChange());
		em.setDrawChange(epl.getDrawChange());
		em.setLoseChange(epl.getLoseChange());
		em.setSmWinDiff(epl.getSmWinDiff());
		em.setSmDrawDiff(epl.getSmDrawDiff());
		em.setSmLoseDiff(epl.getSmLoseDiff());
		return em;
	}
	
	public static MatchExchangeData fromEExchange (EExchange exchange) {
		MatchExchangeData exg = new MatchExchangeData();
		exg.setJcWinExchange(exchange.getJcWinExchange());
		exg.setJcWinExgRt(exchange.getJcWinExgRt());
		exg.setJcWinGain(exchange.getJcWinGain());
		exg.setJcDrawExchange(exchange.getJcDrawExchange());
		exg.setJcDrawExgRt(exchange.getJcDrawExgRt());
		exg.setJcDrawGain(exchange.getJcDrawGain());
		exg.setJcLoseExchange(exchange.getJcLoseExchange());
		exg.setJcLoseExgRt(exchange.getJcLoseExgRt());
		exg.setJcLoseGain(exchange.getJcLoseGain());
		exg.setBfWinExchange(exchange.getBfWinExchange());
		exg.setBfWinExgRt(exchange.getBfWinExgRt());
		exg.setBfWinGain(exchange.getBfWinGain());
		exg.setBfDrawExchange(exchange.getBfDrawExchange());
		exg.setBfDrawExgRt(exchange.getBfDrawExgRt());
		exg.setBfDrawGain(exchange.getBfDrawGain());
		exg.setBfLoseExchange(exchange.getBfLoseExchange());
		exg.setBfLoseExgRt(exchange.getBfLoseExgRt());
		exg.setBfLoseGain(exchange.getBfLoseGain());
		
		return exg;
	}
	
	public static JiaoShouMatrices fromEJiaoShou (EJiaoShou eJiaoShou) {
		JiaoShouMatrices jsMatrices = new JiaoShouMatrices();

		jsMatrices.setLatestPankou(eJiaoShou.getLatestPankou());
		jsMatrices.setLatestDaxiao(eJiaoShou.getLatestPankou());
		jsMatrices.setMatchNum(eJiaoShou.getMatchNum());
		jsMatrices.setHgoalPerMatch(eJiaoShou.getHgoalPerMatch());
		jsMatrices.setGgoalPerMatch(eJiaoShou.getGgoalPerMatch());
		jsMatrices.setWinRate(eJiaoShou.getWinRate());
		jsMatrices.setWinPkRate(eJiaoShou.getWinPkRate());
		jsMatrices.setWinDrawRate(eJiaoShou.getWinDrawRate());
		jsMatrices.setWinDrawPkRate(eJiaoShou.getWinDrawPkRate());
		
		return jsMatrices;
	}
	
	public static MatchState fromELatestMatchState (ELatestMatchState eMatchState) {
		MatchState ms = new MatchState();

		ms.setCalculatePk(eMatchState.getPkByLatestMatches());
		ms.setHotPoint(eMatchState.getHotPoint());
		ms.setHostAttackToGuest(eMatchState.getHostAttackToGuest());
		ms.setHostAttackVariationToGuest(eMatchState.getHostAttackVariationToGuest());
		ms.setGuestAttackToHost(eMatchState.getGuestAttackToHost());
		ms.setGuestAttackVariationToHost(eMatchState.getGuestAttackVariationToHost());
		
		Set<ELatestMatchDetail> latestDetails = eMatchState.getLatestDetails();
		
		for (ELatestMatchDetail emd : latestDetails) {
			LatestMatchMatrixType type = emd.getType();
			
			switch (type) {
				case host6: ms.setHostState6(fromELatestMatchDetail(emd)); break;
				case host10: ms.setHostState10(fromELatestMatchDetail(emd)); break;
				case guest6: ms.setGuestState6(fromELatestMatchDetail(emd)); break;
				case guest10: ms.setGuestState10(fromELatestMatchDetail(emd)); break;
			}
		}
		
		return ms;
	}
	
	public static LatestMatchMatrices fromELatestMatchDetail (ELatestMatchDetail latestDetail) {
		LatestMatchMatrices matrix = new LatestMatchMatrices();

		matrix.setPoint(latestDetail.getPoint());
		matrix.setMatchGoal(latestDetail.getMatchGoal());
		matrix.setMatchMiss(latestDetail.getMatchMiss());
		matrix.setgVariation(latestDetail.getGVariation());
		matrix.setmVariation(latestDetail.getMVariation());
		matrix.setWinRate(latestDetail.getWinRate());
		matrix.setWinPkRate(latestDetail.getWinPkRate());
		matrix.setWinDrawRate(latestDetail.getWinDrawRate());
		matrix.setWinDrawPkRate(latestDetail.getWinDrawPkRate());
		
		return matrix;
	}
	
	public static ClubMatrices toEMatchClubState (Long ofnMatchId, Long hostId, Long guestId, EMatchClubState eMatchClubState) {
		ClubMatrices clubState = new ClubMatrices();
		
		Lists.newArrayList(eMatchClubState.getHostLabel().split(","));

		clubState.setHostLevel(eMatchClubState.getHostLevel());
		clubState.setHostLabels(ConvertHelper.strToLabel(eMatchClubState.getHostLabel()));
		clubState.setGuestLevel(eMatchClubState.getGuestLevel());
		clubState.setGuestLabels(ConvertHelper.strToLabel(eMatchClubState.getGuestLabel()));
		clubState.setHostAttGuestDefInx(eMatchClubState.getHostAttGuestDefInx());
		clubState.setGuestAttHostDefInx(eMatchClubState.getGuestAttHostDefInx());
		
		Set<EMatchClubDetail> clubDetails = eMatchClubState.getClubStateDetails();
		for (EMatchClubDetail cd : clubDetails) {
			MatrixType type = cd.getType();
			Long teamId = cd.getTeamId();
			boolean host = false;
			
			if (hostId.equals(teamId)) {
				host = true;
			}
			
			switch (type) {
				case All:
					if (host) clubState.setHostAllMatrix(fromEMatchClubDetail(cd));
					else clubState.setGuestAllMatrix(fromEMatchClubDetail(cd));
				 	break;
				case Home:
					if (host) clubState.setHostHomeMatrix(fromEMatchClubDetail(cd));
					else clubState.setGuestHomeMatrix(fromEMatchClubDetail(cd));
			 		break;
				case Away:
					if (host) clubState.setHostAwayMatrix(fromEMatchClubDetail(cd));
					else clubState.setGuestAwayMatrix(fromEMatchClubDetail(cd));
			 		break;
			}
		}
		
		return clubState;
	}
	
	public static ClubMatrix fromEMatchClubDetail (EMatchClubDetail clubDetail) {
		ClubMatrix clubMatrix = new ClubMatrix();
		clubMatrix.setNum(clubDetail.getNum());
		clubMatrix.setGoals(clubDetail.getGoals());
		clubMatrix.setMisses(clubDetail.getMisses());
		clubMatrix.setPoint(clubDetail.getPoint());
		clubMatrix.setPm(clubDetail.getPm());
		clubMatrix.setWinRt(clubDetail.getWinRt());
		clubMatrix.setWinDrawRt(clubDetail.getWinDrawRt());
		clubMatrix.setDrawLoseRt(clubDetail.getDrawLoseRt());
		clubMatrix.setWinGoals(clubDetail.getWinGoals());
		clubMatrix.setWinLoseDiff(clubDetail.getWinLoseDiff());
		
		return clubMatrix;
	}
	
	public static MatchResult fromEMatchResult (EMatchResult eResult) {
		MatchResult result = new MatchResult();
		result.setOfnMatchId(eResult.getOfnMatchId());
		result.setHostScore(eResult.getHostScore());
		result.setGuestScore(eResult.getGuestScore());
		
		EMatchResultDetail detail = eResult.getEMatchResultDetail();
		result.setHostId(detail.getHostId());
		result.setHostName(detail.getHostName());
//		result.setHostScore(detail.getHostScore());
		result.setHostShot(detail.getHostShot());
		result.setHostShotOnTarget(detail.getHostShotOnTarget());
		result.setHostSave(detail.getHostSave());
		result.setHostCorner(detail.getHostCorner());
		result.setHostFault(detail.getHostFault());
		result.setHostOffside(detail.getHostOffside());
		result.setHostTime(detail.getHostTime());
		
		result.setGuestId(detail.getGuestId());
		result.setGuestName(detail.getGuestName());
//		result.setGuestScore(detail.getGuestScore());
		result.setGuestShot(detail.getGuestShot());
		result.setGuestShotOnTarget(detail.getGuestShotOnTarget());
		result.setGuestSave(detail.getGuestSave());
		result.setGuestCorner(detail.getGuestCorner());
		result.setGuestFault(detail.getGuestFault());
		result.setGuestOffside(detail.getGuestOffside());
		result.setGuestTime(detail.getGuestTime());
		
		return result;
	}

	public static Float fromEPredictResult (EPredictResult predict) {
		return predict.getPredictPk();
	}
}
