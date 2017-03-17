package com.roy.football.match.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.EMatch;

@Repository
public interface MatchRepository extends CrudRepository<EMatch, Long>{

	@Query("select m from EMatch m where m.phase is null or m.phase < 2")
	public List<EMatch> findMatchesWithoutResult ();
	
	@Query("select m from EMatch m"
			+ "      left join EEuroPlCompany pl on m.ofnMatchId = pl.ofnMatchId"
			+ " where m.matchTime > '2016-08-01 00:00:00'"
			+ "   and m.phase = 2"
			+ "   and m.league != 'Friendly'"
			+ " group by m.ofnMatchId"
			+ " having count(pl.ofnMatchId) < 1")
	public List<EMatch> findMatchesWithoutEuro ();
}
