package com.roy.football.match.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.roy.football.match.jpa.entities.calculation.ETeam;

@Repository
@Transactional(readOnly = true, value = "transactionManager")
public interface TeamRepository extends RoyRepository<ETeam, Long>{
	@Query("select t from ETeam t where t.enTeamName is null")
	public List<ETeam> findTeamsWithoutName ();
}
