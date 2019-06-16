package com.roy.football.match.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.interval.WilsonScoreInterval;

import com.roy.football.match.OFN.parser.MatchParseException;
import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.context.MatchContext;
import com.roy.football.match.crawler.controller.OFNMatchService;

public class Main {
	
	public static void main (String []  args) throws MatchParseException {
//		OFNMatchService simpleController = new OFNMatchService();
//		
//		simpleController.process();
//		simpleController.processMatch(924938l, 160520007l, League.Argintina);
		wilsonScore(100, 2);
		wilsonScore(10000, 100);
		wilsonScore(600, 500);
		wilsonScore(600, 200);
		wilsonScore(600, 60);
		
		wilsonScore(800, 480);
		wilsonScore(400, 250);
		wilsonScore(400, 240);
		wilsonScore(300, 190);
		wilsonScore(200, 120);
		wilsonScore(100, 60);
		wilsonScore(10, 6);
		
		wilsonScore(600, 600);
		wilsonScore(420, 420);
		wilsonScore(400, 400);
		wilsonScore(200, 200);
		wilsonScore(100, 100);
		wilsonScore(60, 60);
		wilsonScore(6, 6);
		wilsonScore(2000000, 1);
	}
	
	private static void wilsonScore (int trails, int success) {
		double d = new WilsonScoreInterval().createInterval(trails, success, 0.95f)
			      .getLowerBound();
		System.out.println(String.format("%3d \t %3d \t %f", trails, success, d));
	}
}
