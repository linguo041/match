package com.roy.football.match.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.roy.football.match.jpa.entities.calculation.EMatch;

@Repository
@Transactional(readOnly = true, value = "transactionManager")
public interface MatchRepository extends RoyRepository<EMatch, Long>{

//	@Query("select m from EMatch m where m.phase = 2 and m.matchTime > '2016-08-01 00:00:00' and m.league != 'Friendly' ")
	@Query("select m from EMatch m where m.phase is null or m.phase < 2")
	public List<EMatch> findMatchesWithoutResult ();
	
	@Query("select m from EMatch m"
			+ "      join EEuroPlState pls on m.ofnMatchId = pls.ofnMatchId"
			+ " where m.matchTime > '2018-08-01 00:00:00'"
			+ "   and m.phase = 2"
			+ "   and m.league != 'Friendly'"
			+ " group by m.ofnMatchId"
			+ " having size(pls.companyPls) < 1")
	public List<EMatch> findMatchesWithoutEuro ();
}
