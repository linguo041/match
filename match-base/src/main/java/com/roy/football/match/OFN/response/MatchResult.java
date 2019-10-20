package com.roy.football.match.OFN.response;

import java.util.Date;

import com.roy.football.match.base.League;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MatchResult {

	private Long ofnMatchId;
	private League league;
	private Long hostId;
	private String hostName;
	private Long guestId;
	private String guestName;
	private Integer hostScore;
	private Integer guestScore;
	private Integer hostShot;
	private Integer guestShot;
	private Integer hostShotOnTarget;
	private Integer guestShotOnTarget;
	private Integer hostFault;
	private Integer guestFault;
	private Integer hostCorner;
	private Integer guestCorner;
	private Integer hostOffside;
	private Integer guestOffside;
	private Integer hostYellowCard;
	private Integer guestYellowCard;
	private Float hostTime;
	private Float guestTime;
	private Integer hostSave;
	private Integer guestSave;
}
