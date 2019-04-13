package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.ELatestMatchState;

@Repository
public interface LatestMatchStateRepository extends RoyRepository<ELatestMatchState, Long> {

}
