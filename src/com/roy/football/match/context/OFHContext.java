package com.roy.football.match.context;

import com.roy.football.match.OFN.parser.OFHConverter;

public class OFHContext implements MatchContext{
	private static OFHContext ofnContext = null;
	
	private OFHContext () {
		init();
	}
	
	public synchronized static MatchContext getMatchContext() {
		if (ofnContext == null) {
			ofnContext = new OFHContext();
		}

		return ofnContext;
	}

	@Override
	public void init() {
		OFHConverter.registerOFHJsonConverter();
	}

	

}
