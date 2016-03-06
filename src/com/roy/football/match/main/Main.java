package com.roy.football.match.main;

import com.roy.football.match.base.League;
import com.roy.football.match.context.MatchContext;
import com.roy.football.match.context.OFHContext;
import com.roy.football.match.crawler.controller.SimpleOFNController;

public class Main {
	
	public static void main (String []  args) {
		MatchContext context = OFHContext.getMatchContext();
		SimpleOFNController simpleController = new SimpleOFNController(context);
		
		simpleController.process();
//		simpleController.processMatch(880857l, League.YiJia);
	}
}
