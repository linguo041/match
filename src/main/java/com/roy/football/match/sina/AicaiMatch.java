package com.roy.football.match.sina;

import java.util.Date;

import lombok.Data;

@Data
public class AicaiMatch {

	private Long fixId;
	private String matchDate;
	private String matchTime;
	private String wholeScore;
	private String homeTeam;
	private String guestTeam;
}
