package com.roy.football.match.OFN.response;

import java.util.Date;

import lombok.Data;

@Data
public class OFNMatchGeneralData {
	private Long mid;
	private Long xid;
	private String weather;
	private Long mtime;
	private Long lid;
	
	private String home;
	private Long htid;
	
	private String away;
	private Long atid;
}
