package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.EMatchResult;

@Repository
public interface MatchResultRepository extends RoyRepository<EMatchResult, Long>{

}
