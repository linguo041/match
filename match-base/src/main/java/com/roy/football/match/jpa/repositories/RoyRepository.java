package com.roy.football.match.jpa.repositories;

import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
@Transactional(readOnly = true, value = "transactionManager")
public interface RoyRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

}
