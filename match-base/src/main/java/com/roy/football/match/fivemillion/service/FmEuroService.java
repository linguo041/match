package com.roy.football.match.fivemillion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQueryFactory;
import com.roy.football.match.jpa.repositories.MatchRepository;

@Service
public class FmEuroService {

	@Autowired
	private JPAQueryFactory jpaQueryFactory;
	
	@Autowired
	private MatchRepository matchRepository;
	
	public void get(){}
}
