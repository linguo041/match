package com.roy.football.match.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roy.football.match.process.machineLearning.linear.MatchAllFactorAnalyzer;
import com.roy.football.match.process.machineLearning.linear.MatchBaseFactorAnalyzer;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PredictScoreFactorClusterService {

	@Autowired
	private MatchAllFactorAnalyzer matchAllFactorAnalyzer;
	
	@Autowired
	private MatchBaseFactorAnalyzer matchBaseFactorAnalyzer;
	
	public void evaluateAllFactors () {
		matchAllFactorAnalyzer.analysis();
	}
	
	public void evaluateBaseFactor () {
		matchBaseFactorAnalyzer.analysis();
	}
}
