package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.jpa.entities.calculation.EAsiaPk;
import com.roy.football.match.jpa.entities.calculation.ELeague;

@Repository
public interface ELeagueRepository extends CrudRepository<ELeague, Long>{
}
