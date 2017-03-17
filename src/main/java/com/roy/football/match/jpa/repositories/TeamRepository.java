package com.roy.football.match.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.ETeam;

@Repository
public interface TeamRepository extends CrudRepository<ETeam, Long>{
	@Query("select t from ETeam t where t.enTeamName is null")
	public List<ETeam> findTeamsWithoutName ();
}
