package com.roy.football.match.jpa.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roy.football.match.OFN.CalculationType;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.CalculatedAndResult;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.jpa.EntiryReverseConverter;
import com.roy.football.match.jpa.EntityConverter;
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
import com.roy.football.match.jpa.entities.calculation.EPredictResult;
import com.roy.football.match.jpa.repositories.AsiaPkRepository;
import com.roy.football.match.jpa.repositories.DaXiaoPkRepository;
import com.roy.football.match.jpa.repositories.EuroPlCompanyRepository;
import com.roy.football.match.jpa.repositories.EuroPlStateRepository;
import com.roy.football.match.jpa.repositories.ExchangeRepository;
import com.roy.football.match.jpa.repositories.JiaoShouRepository;
import com.roy.football.match.jpa.repositories.LatestMatchDetailRepository;
import com.roy.football.match.jpa.repositories.LatestMatchStateRepository;
import com.roy.football.match.jpa.repositories.MatchClubDetailRepository;
import com.roy.football.match.jpa.repositories.MatchClubStateRepository;
import com.roy.football.match.jpa.repositories.MatchRepository;
import com.roy.football.match.jpa.repositories.MatchResultRepository;
import com.roy.football.match.jpa.repositories.PredictResultRepository;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.util.MatchUtil;

@Service
public class MatchPersistService {

	@Autowired
	private AsiaPkRepository asiaPkRepository;
	@Autowired
	private DaXiaoPkRepository daXiaoPkRepository;
	@Autowired
	private EuroPlCompanyRepository euroPlCompanyRepository;
	@Autowired
	private EuroPlStateRepository euroPlStateRepository;
	@Autowired
	private ExchangeRepository exchangeRepository;
	@Autowired
	private JiaoShouRepository jiaoShouRepository;
	
	@Autowired
	private LatestMatchDetailRepository latestMatchDetailRepository;
	@Autowired
	private LatestMatchStateRepository latestMatchStateRepository;
	
	@Autowired
	private MatchClubDetailRepository matchClubDetailRepository;
	@Autowired
	private MatchClubStateRepository matchClubStateRepository;
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private MatchResultRepository matchResultRepository;
	
	@Autowired
	private PredictResultRepository predictResultRepository;
	
	public void save (OFNMatchData ofnMatch, OFNCalculateResult ofnCalculateResult, boolean finished) {
		if (ofnMatch == null || ofnCalculateResult == null) {
			return;
		}
		
		Long ofnMatchId = ofnMatch.getMatchId();
		
		EMatch eMatch = matchRepository.findOne(ofnMatchId);
		if (eMatch != null) {
			CalculationType phase = eMatch.getPhase();
			
			// TODO - if the formula has changed, always re-calculate
			if (phase != null && phase.ordinal() >= CalculationType.resulted.ordinal()) {
				return;
			}
		}
		matchRepository.save(EntityConverter.toEMatch(ofnMatch, CalculationType.calculated));
		
		MatchState matchState = ofnCalculateResult.getMatchState();
		if (!finished && matchState != null && matchState.getHostAttackToGuest() != null) {
			ELatestMatchState eLatestMatchState = EntityConverter.toELatestMatchState(ofnMatchId,
					ofnMatch.getLeague(), matchState);
			latestMatchStateRepository.save(eLatestMatchState);
			
//			for (ELatestMatchDetail eLatestMatchDetail : eLatestMatchState.getLatestDetails()) {
//				if (eLatestMatchDetail != null) {
//					latestMatchDetailRepository.save(eLatestMatchDetail);
//				}
//			}
		}
		
		JiaoShouMatrices jsMatrices = ofnCalculateResult.getJiaoShou();
		Float latestPk = null;
		if (!finished && jsMatrices != null) {
			latestPk = jsMatrices.getLatestPankou();
			jiaoShouRepository.save(EntityConverter.toEJiaoShou(ofnMatchId, jsMatrices));
		}
		
		ClubMatrices clubMatrices = ofnCalculateResult.getClubMatrices();
		if (!finished && clubMatrices != null) {
			EMatchClubState eMatchClubState = EntityConverter.toEMatchClubState(ofnMatchId,
					ofnMatch.getLeague(), ofnMatch.getHostId(), ofnMatch.getGuestId(), clubMatrices);

			matchClubStateRepository.save(eMatchClubState);
			
//			for (EMatchClubDetail eMatchClubDetail : eMatchClubState.getClubStateDetails()) {
//				if (eMatchClubDetail != null) {
//					matchClubDetailRepository.save(eMatchClubDetail);
//				}
//			}
		}
		
		MatchExchangeData matchExchangeData = ofnCalculateResult.getExchanges();
		if (matchExchangeData != null) {
			exchangeRepository.save(EntityConverter.toEExchange(ofnMatchId, matchExchangeData));
		}
		
		PankouMatrices pkMatrices = ofnCalculateResult.getPkMatrices();
		if (pkMatrices != null) {
			asiaPkRepository.save(EntityConverter.toEAsiaPk(ofnMatchId, pkMatrices, Company.Aomen));
		}
		
		PankouMatrices ysbPkMatrices = ofnCalculateResult.getYsbPkMatrices();
		if (ysbPkMatrices != null) {
			asiaPkRepository.save(EntityConverter.toEAsiaPk(ofnMatchId, ysbPkMatrices, Company.YiShenBo));
		}
		
		DaxiaoMatrices dxMatrices = ofnCalculateResult.getDxMatrices();
		if (dxMatrices != null) {
			daXiaoPkRepository.save(EntityConverter.toEDaXiaoPk(ofnMatchId, dxMatrices));
		}
		
		EuroMatrices euroMatrices = ofnCalculateResult.getEuroMatrices();
		if (euroMatrices != null) {
			EEuroPlState eEuroPlState = EntityConverter.toEEuroPlState(ofnMatchId, euroMatrices);
			euroPlStateRepository.save(eEuroPlState);
			
//			for (EEuroPlCompany eEuroPlCompany : eEuroPlState.getCompanyPls()) {
//				if (eEuroPlCompany != null) {
//					euroPlCompanyRepository.save(eEuroPlCompany);
//				}
//			}
		}
		
		Float predictPk = ofnCalculateResult.getPredictPanKou();
		if (predictPk != null) {
			Float hostScore = null;
			Float guestScore = null;
			PredictResult predictRes = ofnCalculateResult.getPredictResult();
			
			if (predictRes != null) {
				hostScore = predictRes.getHostScore();
				guestScore = predictRes.getGuestScore();
			}
			
			EPredictResult predict = EntityConverter.toEPredictResult(ofnMatchId, predictPk, latestPk, hostScore, guestScore);
			
			predictResultRepository.save(predict);
		}
	}
	
	public CalculatedAndResult load (Long matchId) {
		CalculatedAndResult calResult = new CalculatedAndResult();
		
		EMatch ematch  = matchRepository.findOne(matchId);
		OFNMatchData ofnMatch = EntiryReverseConverter.fromEMatch(ematch);
		calResult.setLeague(ofnMatch.getLeague());
		
		ELatestMatchState eLatestMatchState = latestMatchStateRepository.findOne(matchId);
		if (eLatestMatchState != null) {
			calResult.setMatchState(EntiryReverseConverter.fromELatestMatchState(eLatestMatchState));
		}
		
		EJiaoShou ejs = jiaoShouRepository.findOne(matchId);
		if (ejs != null) {
			calResult.setJiaoShou(EntiryReverseConverter.fromEJiaoShou(ejs));
		}
		
		EMatchClubState eMatchClubState = matchClubStateRepository.findOne(matchId);
		if (eMatchClubState != null) {
			calResult.setClubMatrices(EntiryReverseConverter.fromEMatchClubState(matchId,
					ofnMatch.getHostId(), ofnMatch.getGuestId(), eMatchClubState));
		}
		
		EExchange eExchange = exchangeRepository.findOne(matchId);
		if (eExchange != null) {
			calResult.setExchanges(EntiryReverseConverter.fromEExchange(eExchange));
		}
		
		EAsiaPk eAsiaPk = asiaPkRepository.findByOfnMatchIdAndCompany(matchId, Company.Aomen);
		if (eAsiaPk != null) {
			calResult.setPkMatrices(EntiryReverseConverter.fromEAsiaPk(eAsiaPk));
		}
		EAsiaPk ysb = asiaPkRepository.findByOfnMatchIdAndCompany(matchId, Company.YiShenBo);
		if (ysb != null) {
			calResult.setYsbPkMatrices(EntiryReverseConverter.fromEAsiaPk(ysb));
		}
		
		EDaXiaoPk eDaXiaoPk = daXiaoPkRepository.findOne(matchId);
		if (eDaXiaoPk != null) {
			calResult.setDxMatrices(EntiryReverseConverter.fromEDaXiaoPk(eDaXiaoPk));
		}
		
		EEuroPlState eEuroPlState = euroPlStateRepository.findOne(matchId);
		if (eEuroPlState != null) {
			calResult.setEuroMatrices(EntiryReverseConverter.fromEEuroPlState(eEuroPlState));
		}
		
		EPredictResult ePredictResult = predictResultRepository.findOne(matchId);
		if (ePredictResult != null) {
			calResult.setPredictPanKou(EntiryReverseConverter.fromEPredictResult(ePredictResult));
		}
		
		EMatchResult dbResult = matchResultRepository.findOne(matchId);
		if (dbResult != null) {
			calResult.setMatchResult(EntiryReverseConverter.fromEMatchResult(dbResult));
		}
		
		return calResult;
	}
	
	public void saveHistoryMatch (List<FinishedMatch> finishedMatches) {
		if (finishedMatches == null || finishedMatches.size() <= 0) {
			return ;
		}
		
		Date now = new Date();
		for (FinishedMatch fm : finishedMatches) {
			if (MatchUtil.isMatchInTwoYear(fm.getMatchTime(), now)) {
				saveFinishedMatch(fm);
			}
		}
	}
	
	private void saveFinishedMatch (FinishedMatch fm) {
		Long ofnMatchId = fm.getMatchId();
		
		try {
			EMatch oldEMatch = matchRepository.findOne(ofnMatchId);
			
			if (oldEMatch == null) {
				EMatch eMatch = EntityConverter.toEMatch(fm, CalculationType.created);
				matchRepository.save(eMatch);
			}
		} catch (Exception e) {
			// ignore..
		}
	}
}
