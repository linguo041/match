package com.roy.football.match.jpa.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.audit.EEuroAudit;
import com.roy.football.match.jpa.entities.audit.PKType;
import com.roy.football.match.jpa.repositories.EuroAuditRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EuroAnalyzer {

	@Autowired
	private EuroAuditRepository euroAuditRepository;
	
	public EuroPl getLeagueAvgPl (Float pk, Company company, League league) {
		try {
			EEuroAudit euroAudit = euroAuditRepository.findByLeagueIdAndCompanyAndPkAndPkType(league.getLeagueId(),
					company, pk, PKType.Current);
			
			if (euroAudit != null) {
				return new EuroPl(euroAudit.getAvgWin(), euroAudit.getAvgDraw(), euroAudit.getAvgLose(), new Date());
			}
		} catch (Exception e) {
			log.warn("Can't find the euro audit for league {}, company {}, pk {}", league.getLeagueId(), company, pk);
		}
		
		return null;
	}
}
