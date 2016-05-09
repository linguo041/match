package com.roy.football.match.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.context.MatchContext;
import com.roy.football.match.context.OFHContext;
import com.roy.football.match.crawler.controller.SimpleOFNController;

public class Main {
	
	public static void main (String []  args) {
		MatchContext context = OFHContext.getMatchContext();
		SimpleOFNController simpleController = new SimpleOFNController(context);
		
		simpleController.process();
//		simpleController.processMatch(861076l, 160508047l, League.DeYi);
	}
}
