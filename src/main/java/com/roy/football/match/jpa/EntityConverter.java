package com.roy.football.match.jpa;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.roy.football.match.OFN.CalculationType;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.MatchResult;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.LatestMatchMatrixType;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatrixType;
import com.roy.football.match.base.TeamLabel;
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

public class EntityConverter {

	public static EMatch toEMatch (OFNMatchData ofnMatch, CalculationType phase) {
		EMatch ematch = new EMatch();
		ematch.setOfnMatchId(ofnMatch.getMatchId());
		ematch.setLeague(ofnMatch.getLeague());
		ematch.setMatchTime(ofnMatch.getMatchTime());
		ematch.setMatchDayId(ofnMatch.getMatchDayId());
		ematch.setOkoooMatchId(ofnMatch.getOkoooMatchId());
		ematch.setHostId(ofnMatch.getHostId());
		ematch.setHostName(ofnMatch.getHostName());
		ematch.setGuestId(ofnMatch.getGuestId());
		ematch.setGuestName(ofnMatch.getGuestName());
		ematch.setPhase(phase);
		return ematch;
	}
	
	public static EMatch toEMatch (FinishedMatch finishedMatch, CalculationType phase) {
		EMatch ematch = new EMatch();
		ematch.setOfnMatchId(finishedMatch.getMatchId());
		ematch.setLeague(League.getLeagueById(finishedMatch.getLeagueId()));
		ematch.setMatchTime(finishedMatch.getMatchTime());
//		ematch.setMatchDayId(finishedMatch.getMatchDayId());
//		ematch.setOkoooMatchId(finishedMatch.getOkoooMatchId());
		ematch.setHostId(finishedMatch.getHostId());
		ematch.setHostName(finishedMatch.getHostName());
		ematch.setGuestId(finishedMatch.getGuestId());
		ematch.setGuestName(finishedMatch.getGuestName());
		ematch.setPhase(phase);
		return ematch;
	}
	
	public static EAsiaPk toEAsiaPk (Long ofnMatchId, PankouMatrices pkMatrics, Company company) {
		EAsiaPk easiaPk = new EAsiaPk();
		easiaPk.setOfnMatchId(ofnMatchId);
		easiaPk.setCompany(company);
		
		AsiaPl main = pkMatrics.getMainPk();
		easiaPk.setMainPk(main.getPanKou());
		easiaPk.setMainHWin(main.gethWin());
		easiaPk.setMainAWin(main.getaWin());
		
		AsiaPl original = pkMatrics.getOriginPk();
		easiaPk.setOriginPk(original.getPanKou());
		easiaPk.setOriginHWin(original.gethWin());
		easiaPk.setOriginAWin(original.getaWin());
		
		AsiaPl current = pkMatrics.getCurrentPk();
		easiaPk.setCurrentPk(current.getPanKou());
		easiaPk.setCurrentHWin(current.gethWin());
		easiaPk.setCurrentAWin(current.getaWin());
		
		easiaPk.setHwinChangeRate(pkMatrics.getHwinChangeRate());
		easiaPk.setAwinChangeRate(pkMatrics.getAwinChangeRate());
		easiaPk.setHours(pkMatrics.getHours());
		
		return easiaPk;
	}
	
	public static EDaXiaoPk toEDaXiaoPk (Long ofnMatchId, DaxiaoMatrices dxMatrics) {
		EDaXiaoPk dxPk = new EDaXiaoPk();
		dxPk.setOfnMatchId(ofnMatchId);
		
		AsiaPl main = dxMatrics.getMainPk();
		dxPk.setMainPk(main.getPanKou());
		dxPk.setMainHWin(main.gethWin());
		dxPk.setMainAWin(main.getaWin());
		
		AsiaPl original = dxMatrics.getOriginPk();
		dxPk.setOriginPk(original.getPanKou());
		dxPk.setOriginHWin(original.gethWin());
		dxPk.setOriginAWin(original.getaWin());
		
		AsiaPl current = dxMatrics.getCurrentPk();
		dxPk.setCurrentPk(current.getPanKou());
		dxPk.setCurrentHWin(current.gethWin());
		dxPk.setCurrentAWin(current.getaWin());
		
		dxPk.setDaChangeRate(dxMatrics.getDaChangeRate());
		dxPk.setXiaoChangeRate(dxMatrics.getXiaoChangeRate());
		dxPk.setHours(dxMatrics.getHours());
		
		return dxPk;
	}
	
	public static EEuroPlState toEEuroPlState (Long ofnMatchId, EuroMatrices euroMatrices) {
		EEuroPlState eEuroPlState = new EEuroPlState();
		
		eEuroPlState.setOfnMatchId(ofnMatchId);
		eEuroPlState.setMainAvgWinDiff(euroMatrices.getMainAvgWinDiff());
		eEuroPlState.setMainAvgDrawDiff(euroMatrices.getMainAvgDrawDiff());
		eEuroPlState.setMainAvgLoseDiff(euroMatrices.getMainAvgLoseDiff());
		
		Set<EEuroPlCompany> euCompanyPls = Sets.newHashSet();
		eEuroPlState.setCompanyPls(euCompanyPls);

		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		for (Map.Entry<Company, EuroMatrix> entry : companyEus.entrySet()) {
			if (entry.getValue() != null) {
				euCompanyPls.add(toEEuroPlCompany(ofnMatchId, entry.getKey(), entry.getValue()));
			}
		}
		
		EuroPl avg = euroMatrices.getCurrEuroAvg();
		if (avg != null) {
			eEuroPlState.setAvgWin(avg.getEWin());
			eEuroPlState.setAvgDraw(avg.getEDraw());
			eEuroPlState.setAvgLose(avg.getELose());
		}
	
		return eEuroPlState;
	}
	
	public static EEuroPlCompany toEEuroPlCompany (Long ofnMatchId, Company company, EuroMatrix euroMatrix) {
		if (euroMatrix == null) {
			return null;
		}
		
		EEuroPlCompany eEuroPlCompany = new EEuroPlCompany();
		eEuroPlCompany.setCompany(company);
		eEuroPlCompany.setOfnMatchId(ofnMatchId);
		
		eEuroPlCompany.setWinChange(euroMatrix.getWinChange());
		eEuroPlCompany.setDrawChange(euroMatrix.getDrawChange());
		eEuroPlCompany.setLoseChange(euroMatrix.getLoseChange());
		
		EuroPl origin = euroMatrix.getOriginEuro();
		if (origin != null) {
			eEuroPlCompany.setOriginEWin(origin.getEWin());
			eEuroPlCompany.setOriginEDraw(origin.getEDraw());
			eEuroPlCompany.setOriginELose(origin.getELose());
		}
		
		EuroPl current = euroMatrix.getCurrentEuro();
		if (current != null) {
			eEuroPlCompany.setCurrentEWin(current.getEWin());
			eEuroPlCompany.setCurrentEDraw(current.getEDraw());
			eEuroPlCompany.setCurrentELose(current.getELose());
		}
		
		EuroPl main = euroMatrix.getMainEuro();
		if (main != null) {
			eEuroPlCompany.setMainEWin(main.getEWin());
			eEuroPlCompany.setMainEDraw(main.getEDraw());
			eEuroPlCompany.setMainELose(main.getELose());
		}
		
		eEuroPlCompany.setSmWinDiff(euroMatrix.getSmWinDiff());
		eEuroPlCompany.setSmDrawDiff(euroMatrix.getSmDrawDiff());
		eEuroPlCompany.setSmLoseDiff(euroMatrix.getSmLoseDiff());
		
		return eEuroPlCompany;
	}
	
	public static EExchange toEExchange (Long ofnMatchId, MatchExchangeData exchange) {
		EExchange eExchange = new EExchange();
		eExchange.setOfnMatchId(ofnMatchId);
		eExchange.setJcWinExchange(exchange.getJcWinExchange());
		eExchange.setJcWinExgRt(exchange.getJcWinExgRt());
		eExchange.setJcWinGain(exchange.getJcWinGain());
		eExchange.setJcDrawExchange(exchange.getJcDrawExchange());
		eExchange.setJcDrawExgRt(exchange.getJcDrawExgRt());
		eExchange.setJcDrawGain(exchange.getJcDrawGain());
		eExchange.setJcLoseExchange(exchange.getJcLoseExchange());
		eExchange.setJcLoseExgRt(exchange.getJcLoseExgRt());
		eExchange.setJcLoseGain(exchange.getJcLoseGain());
		eExchange.setBfWinExchange(exchange.getBfWinExchange());
		eExchange.setBfWinExgRt(exchange.getBfWinExgRt());
		eExchange.setBfWinGain(exchange.getBfWinGain());
		eExchange.setBfDrawExchange(exchange.getBfDrawExchange());
		eExchange.setBfDrawExgRt(exchange.getBfDrawExgRt());
		eExchange.setBfDrawGain(exchange.getBfDrawGain());
		eExchange.setBfLoseExchange(exchange.getBfLoseExchange());
		eExchange.setBfLoseExgRt(exchange.getBfLoseExgRt());
		eExchange.setBfLoseGain(exchange.getBfLoseGain());
		
		return eExchange;
	}
	
	public static EJiaoShou toEJiaoShou (Long ofnMatchId, JiaoShouMatrices jsMatrices) {
		EJiaoShou eJiaoShou = new EJiaoShou();
		
		eJiaoShou.setOfnMatchId(ofnMatchId);
		eJiaoShou.setLatestPankou(jsMatrices.getLatestPankou());
		eJiaoShou.setLatestDaxiao(jsMatrices.getLatestPankou());
		eJiaoShou.setMatchNum(jsMatrices.getMatchNum());
		eJiaoShou.setHgoalPerMatch(jsMatrices.getHgoalPerMatch());
		eJiaoShou.setGgoalPerMatch(jsMatrices.getGgoalPerMatch());
		eJiaoShou.setWinRate(jsMatrices.getWinRate());
		eJiaoShou.setWinPkRate(jsMatrices.getWinPkRate());
		eJiaoShou.setWinDrawRate(jsMatrices.getWinDrawRate());
		eJiaoShou.setWinDrawPkRate(jsMatrices.getWinDrawPkRate());
		
		return eJiaoShou;
	}
	
	public static ELatestMatchState toELatestMatchState (Long ofnMatchId, League league, MatchState matchState) {
		ELatestMatchState eLatestMatchState = new ELatestMatchState();
		
		eLatestMatchState.setOfnMatchId(ofnMatchId);
		eLatestMatchState.setLeague(league);
		eLatestMatchState.setPkByLatestMatches(matchState.getCalculatePk());
		eLatestMatchState.setHotPoint(matchState.getHotPoint());
		eLatestMatchState.setHostAttackToGuest(matchState.getHostAttackToGuest());
		eLatestMatchState.setHostAttackVariationToGuest(matchState.getHostAttackVariationToGuest());
		eLatestMatchState.setGuestAttackToHost(matchState.getGuestAttackToHost());
		eLatestMatchState.setGuestAttackVariationToHost(matchState.getGuestAttackVariationToHost());
		
		Set<ELatestMatchDetail> latestDetails = Sets.newHashSet();
		ELatestMatchDetail detailH6 = toLatestMatchMatrices(ofnMatchId, LatestMatchMatrixType.host6, matchState.getHostState6());
		ELatestMatchDetail detailH10 = toLatestMatchMatrices(ofnMatchId, LatestMatchMatrixType.host10, matchState.getHostState10());
		ELatestMatchDetail detailG6 = toLatestMatchMatrices(ofnMatchId, LatestMatchMatrixType.guest6, matchState.getGuestState6());
		ELatestMatchDetail detailG10 = toLatestMatchMatrices(ofnMatchId, LatestMatchMatrixType.guest10, matchState.getGuestState10());
		if (detailH6 != null) {
			latestDetails.add(detailH6);
		}
		if (detailH10 != null) {
			latestDetails.add(detailH10);
		}
		if (detailG6 != null) {
			latestDetails.add(detailG6);
		}
		if (detailG10 != null) {
			latestDetails.add(detailG10);
		}

		eLatestMatchState.setLatestDetails(latestDetails);
		
		return eLatestMatchState;
	}
	
	public static ELatestMatchDetail toLatestMatchMatrices (Long ofnMatchId, LatestMatchMatrixType type, LatestMatchMatrices latestMatchMatrices) {
		if (latestMatchMatrices == null) {
			return null;
		}
		
		ELatestMatchDetail eLatestMatchDetail = new ELatestMatchDetail();
		
		eLatestMatchDetail.setOfnMatchId(ofnMatchId);
		eLatestMatchDetail.setType(type);
		eLatestMatchDetail.setPoint(latestMatchMatrices.getPoint());
		eLatestMatchDetail.setMatchGoal(latestMatchMatrices.getMatchGoal());
		eLatestMatchDetail.setMatchMiss(latestMatchMatrices.getMatchMiss());
		eLatestMatchDetail.setGVariation(latestMatchMatrices.getgVariation());
		eLatestMatchDetail.setMVariation(latestMatchMatrices.getmVariation());
		eLatestMatchDetail.setWinRate(latestMatchMatrices.getWinRate());
		eLatestMatchDetail.setWinPkRate(latestMatchMatrices.getWinPkRate());
		eLatestMatchDetail.setWinDrawRate(latestMatchMatrices.getWinDrawRate());
		eLatestMatchDetail.setWinDrawPkRate(latestMatchMatrices.getWinDrawPkRate());
		
		return eLatestMatchDetail;
	}
	
	public static EMatchClubState toEMatchClubState (Long ofnMatchId, League league, Long hostId, Long guestId, ClubMatrices clubMatrices) {
		EMatchClubState eMatchClubState = new EMatchClubState();
		eMatchClubState.setOfnMatchId(ofnMatchId);
		eMatchClubState.setLeague(league);
		eMatchClubState.setHostId(hostId);
		eMatchClubState.setGuestId(guestId);
		eMatchClubState.setHostLevel(clubMatrices.getHostLevel());
		eMatchClubState.setHostLabel(clubMatrices.getHostLabels().toString());
		eMatchClubState.setGuestLevel(clubMatrices.getGuestLevel());
		eMatchClubState.setGuestLabel(clubMatrices.getGuestLabels().toString());
		eMatchClubState.setHostAttGuestDefInx(clubMatrices.getHostAttGuestDefInx());
		eMatchClubState.setGuestAttHostDefInx(clubMatrices.getGuestAttHostDefInx());
		
		Set<EMatchClubDetail> clubDetails = Sets.newHashSet();
		clubDetails.add(toEMatchClubDetail(ofnMatchId, hostId, MatrixType.All, clubMatrices.getHostAllMatrix()));
		clubDetails.add(toEMatchClubDetail(ofnMatchId, hostId, MatrixType.Home, clubMatrices.getHostHomeMatrix()));
		clubDetails.add(toEMatchClubDetail(ofnMatchId, hostId, MatrixType.Away, clubMatrices.getHostAwayMatrix()));
		clubDetails.add(toEMatchClubDetail(ofnMatchId, guestId, MatrixType.All, clubMatrices.getGuestAllMatrix()));
		clubDetails.add(toEMatchClubDetail(ofnMatchId, guestId, MatrixType.Home, clubMatrices.getGuestHomeMatrix()));
		clubDetails.add(toEMatchClubDetail(ofnMatchId, guestId, MatrixType.Away, clubMatrices.getGuestAwayMatrix()));
		eMatchClubState.setClubStateDetails(clubDetails);
		
		return eMatchClubState;
	}
	
	public static EMatchClubDetail toEMatchClubDetail (Long ofnMatchId, Long teamId, MatrixType type, ClubMatrix clubMatrix) {
		EMatchClubDetail eMatchClubDetail = new EMatchClubDetail();
		eMatchClubDetail.setOfnMatchId(ofnMatchId);
		eMatchClubDetail.setTeamId(teamId);
		eMatchClubDetail.setType(type);
		eMatchClubDetail.setNum(clubMatrix.getNum());
		eMatchClubDetail.setGoals(clubMatrix.getGoals());
		eMatchClubDetail.setMisses(clubMatrix.getMisses());
		eMatchClubDetail.setPoint(clubMatrix.getPoint());
		eMatchClubDetail.setPm(clubMatrix.getPm());
		eMatchClubDetail.setWinRt(clubMatrix.getWinRt());
		eMatchClubDetail.setWinDrawRt(clubMatrix.getWinDrawRt());
		eMatchClubDetail.setDrawLoseRt(clubMatrix.getDrawLoseRt());
		eMatchClubDetail.setWinGoals(clubMatrix.getWinGoals());
		eMatchClubDetail.setWinLoseDiff(clubMatrix.getWinLoseDiff());
		
		return eMatchClubDetail;
	}
	
	public static EMatchResult toEMatchResult (Long ofnMatchId, MatchResult result) {
		EMatchResult dbResult = new EMatchResult();
		dbResult.setOfnMatchId(ofnMatchId);
		dbResult.setHostScore(result.getHostScore());
		dbResult.setGuestScore(result.getGuestScore());
		
		dbResult.setEMatchResultDetail(toEMatchResultDetail(ofnMatchId, result));
		return dbResult;
	}
	
	public static EMatchResultDetail toEMatchResultDetail (Long ofnMatchId, MatchResult result) {
		EMatchResultDetail resultDetail = new EMatchResultDetail();
		resultDetail.setOfnMatchId(ofnMatchId);
		
		resultDetail.setHostId(result.getHostId());
		resultDetail.setHostName(result.getHostName());
		resultDetail.setHostScore(result.getHostScore());
		resultDetail.setHostShot(result.getHostShot());
		resultDetail.setHostShotOnTarget(result.getHostShotOnTarget());
		resultDetail.setHostSave(result.getHostSave());
		resultDetail.setHostCorner(result.getHostCorner());
		resultDetail.setHostFault(result.getHostFault());
		resultDetail.setHostOffside(result.getHostOffside());
		resultDetail.setHostTime(result.getHostTime());
		
		resultDetail.setGuestId(result.getGuestId());
		resultDetail.setGuestName(result.getGuestName());
		resultDetail.setGuestScore(result.getGuestScore());
		resultDetail.setGuestShot(result.getGuestShot());
		resultDetail.setGuestShotOnTarget(result.getGuestShotOnTarget());
		resultDetail.setGuestSave(result.getGuestSave());
		resultDetail.setGuestCorner(result.getGuestCorner());
		resultDetail.setGuestFault(result.getGuestFault());
		resultDetail.setGuestOffside(result.getGuestOffside());
		resultDetail.setGuestTime(result.getGuestTime());
		
		return resultDetail;
	}
	
	public static EPredictResult toEPredictResult (Long ofnMatchId, Float predictPk, Float lastMatchPk, Float hostScore, Float guestScore) {
		EPredictResult pr = new EPredictResult();
		pr.setOfnMatchId(ofnMatchId);
		pr.setLastMatchPk(lastMatchPk);
		pr.setPredictPk(predictPk);
		pr.setHostScore(hostScore);
		pr.setGuestScore(guestScore);
		return pr;
	}
}
