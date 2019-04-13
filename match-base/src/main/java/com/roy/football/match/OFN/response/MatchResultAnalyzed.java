package com.roy.football.match.OFN.response;

import java.util.Date;

import com.roy.football.match.base.League;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MatchResultAnalyzed {

	private Long teamId;
	private Float hostScore;
	private Float guestScore;
	private Float hostShot;
	private Float guestShot;
	private Float hostShotOnTarget;
	private Float guestShotOnTarget;
	private Float hostFault;
	private Float guestFault;
	private Float hostCorner;
	private Float guestCorner;
	private Float hostOffside;
	private Float guestOffside;
	private Float hostYellowCard;
	private Float guestYellowCard;
	private Float hostTime;
	private Float guestTime;
	private Float hostSave;
	private Float guestSave;
}
