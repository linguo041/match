package com.roy.football.match.jpa.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.roy.football.match.jpa.entities.calculation.EEuroPlCompany;
import com.roy.football.match.jpa.entities.calculation.EEuroPlCompany.EEuroPlCompanyPk;

@Repository
public interface EuroPlCompanyRepository extends CrudRepository<EEuroPlCompany, EEuroPlCompany.EEuroPlCompanyPk>{

}
