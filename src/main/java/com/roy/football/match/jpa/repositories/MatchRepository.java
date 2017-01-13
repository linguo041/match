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
}
