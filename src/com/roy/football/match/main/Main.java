package com.roy.football.match.main;

import com.roy.football.match.crawler.controller.SimpleOFNController;
import com.roy.football.match.httpRequest.HttpRequestService;

public class Main {
	
	public static void main (String []  args) {
		SimpleOFNController simpleController = new SimpleOFNController(new HttpRequestService());
		
		simpleController.process();
	}
}
