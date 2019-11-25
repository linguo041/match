package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.roy.football.match.jpa.entities.calculation.EEuroPlState;

@Repository
@Transactional(readOnly = true, value = "transactionManager")
public interface EuroPlStateRepository extends RoyRepository<EEuroPlState, Long> {

}
