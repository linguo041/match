package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.ELatestMatchDetail;

@Repository
public interface LatestMatchDetailRepository extends CrudRepository<ELatestMatchDetail, ELatestMatchDetail.ELatestMatchDetailPk>{

}
