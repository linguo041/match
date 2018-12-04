package com.roy.football.match.fivemillion;

import java.util.Date;

import lombok.Data;

@Data
public class FmMatch {

	private Long fmMatchId;
	private Integer matchOrderId;
	private Long hostId;
	private String hostName;
	private Long guestId;
	private String guestName;
	private Date matchDate;
	private Integer hostScore;
	private Integer guestScore;
}
