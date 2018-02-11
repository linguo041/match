package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.base.League;
import com.roy.football.match.jpa.entities.audit.EEuroAudit;
import com.roy.football.match.jpa.entities.audit.PKType;

@Repository
public interface EuroAuditRepository extends CrudRepository<EEuroAudit, Long>{
	EEuroAudit findByLeagueIdAndCompanyAndPkAndPkType (Long leagueId, Company company, Float pk, PKType type) ;
}
