package com.roy.football.match.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roy.football.match.OFN.statics.matrices.CalculatedAndResult;
import com.roy.football.match.jpa.service.MatchPersistService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatchResultAnalyzeService {
	@Autowired
	private MatchPersistService matchPersistService;
	
	public void analyze (Long matchId) {
		CalculatedAndResult amd = matchPersistService.load(matchId);
		
		
	}
}
