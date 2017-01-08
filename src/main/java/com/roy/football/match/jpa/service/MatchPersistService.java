package com.roy.football.match.jpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.jpa.EntityConverter;
import com.roy.football.match.jpa.entities.calculation.EEuroPlCompany;
import com.roy.football.match.jpa.entities.calculation.EEuroPlState;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchDetail;
import com.roy.football.match.jpa.entities.calculation.ELatestMatchState;
import com.roy.football.match.jpa.entities.calculation.EMatch;
import com.roy.football.match.jpa.entities.calculation.EMatchClubDetail;
import com.roy.football.match.jpa.entities.calculation.EMatchClubState;
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
import com.roy.football.match.okooo.MatchExchangeData;

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
	
	public void save (OFNMatchData ofnMatch, OFNCalculateResult ofnCalculateResult) {
		if (ofnMatch == null || ofnCalculateResult == null) {
			return;
		}
		
		Long ofnMatchId = ofnMatch.getMatchId();
		

		EMatch eMatch = matchRepository.findOne(ofnMatchId);
		
		if (true /*eMatch == null*/) {
			eMatch = EntityConverter.toEMatch(ofnMatch);
			
			matchRepository.save(eMatch);

			PankouMatrices pkMatrices = ofnCalculateResult.getPkMatrices();
			if (pkMatrices != null) {
				asiaPkRepository.save(EntityConverter.toEAsiaPk(ofnMatchId, pkMatrices));
			}
			
			DaxiaoMatrices dxMatrices = ofnCalculateResult.getDxMatrices();
			if (dxMatrices != null) {
				daXiaoPkRepository.save(EntityConverter.toEDaXiaoPk(ofnMatchId, dxMatrices));
			}
			
			EuroMatrices euroMatrices = ofnCalculateResult.getEuroMatrices();
			if (euroMatrices != null) {
				EEuroPlState eEuroPlState = EntityConverter.toEEuroPlState(ofnMatchId, euroMatrices);
				euroPlStateRepository.save(eEuroPlState);
				
				for (EEuroPlCompany eEuroPlCompany : eEuroPlState.getCompanyPls()) {
					if (eEuroPlCompany != null) {
						euroPlCompanyRepository.save(eEuroPlCompany);
					}
				}
			}
			
			ClubMatrices clubMatrices = ofnCalculateResult.getClubMatrices();
			if (clubMatrices != null) {
				EMatchClubState eMatchClubState = EntityConverter.toEMatchClubState(ofnMatchId,
						ofnMatch.getHostId(), ofnMatch.getGuestId(), clubMatrices);
				matchClubStateRepository.save(eMatchClubState);
				
				for (EMatchClubDetail eMatchClubDetail : eMatchClubState.getClubStateDetails()) {
					if (eMatchClubDetail != null) {
						matchClubDetailRepository.save(eMatchClubDetail);
					}
				}
			}
			
			JiaoShouMatrices jsMatrices = ofnCalculateResult.getJiaoShou();
			if (jsMatrices != null) {
				jiaoShouRepository.save(EntityConverter.toEJiaoShou(ofnMatchId, jsMatrices));
			}
			
			MatchExchangeData matchExchangeData = ofnCalculateResult.getExchanges();
			if (matchExchangeData != null) {
				exchangeRepository.save(EntityConverter.toEExchange(ofnMatchId, matchExchangeData));
			}
			
			MatchState matchState = ofnCalculateResult.getMatchState();
			if (matchState != null) {
				ELatestMatchState eLatestMatchState = EntityConverter.toELatestMatchState(ofnMatchId,
						ofnMatch.getLeague(), matchState);
				latestMatchStateRepository.save(eLatestMatchState);
				
				for (ELatestMatchDetail eLatestMatchDetail : eLatestMatchState.getLatestDetails()) {
					if (eLatestMatchDetail != null) {
						latestMatchDetailRepository.save(eLatestMatchDetail);
					}
				}
			}
		}
		

	}
	
	
	
}
