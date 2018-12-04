package com.roy.football.match.OFN.response;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class OFNMatchGeneralData {
	private Long mid;
	@SerializedName("xid1")
	private Long xid;
	@SerializedName("xid")
	private String xidStr;
	private String weather;
	private Long mtime;
	private Long lid;
	
	private String home;
	private Long htid;
	
	private String away;
	private Long atid;
}
