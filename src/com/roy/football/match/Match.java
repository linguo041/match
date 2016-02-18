package com.roy.football.match;

import java.util.Date;

public class Match {
	
	// match data
	private Long matchId;
	private Date matchTime;
	// club data
	private Club host;
	private Club guest;
	// match between each
	private OldMatches oldRecord;
	// host's recent match
	private RecentMatches hostMatches;
	// guest's recent match
	private RecentMatches guestMatches;
	// Aomen pankou
	private Asia aomen;
	// Euro peilv
	private EuroPeilv pl;
	// Game result
	private GameResult result;
}
