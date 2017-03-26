package com.roy.football.match.OFN.response;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchData;

import lombok.Data;

@Data
public class OFNMatchData implements MatchData{

	@Override
	public String toString() {
		return "OFNMatchData [matchDayId=" + matchDayId + ", matchId="
				+ matchId + ", matchTime=" + matchTime + ", league="
				+ league + ", hostId="
				+ hostId + ", hostName=" + hostName + ", guestId=" + guestId
				+ ", guestName=" + guestName
				+ ", baseData=" + baseData + ", jiaoShou=" + jiaoShou
				+ ", hostMatches=" + hostMatches + ", guestMatches="
				+ guestMatches + ", euroPls=" + euroPls + ", aoMen=" + aoMen
				+ "]";
	}

	private Long okoooMatchId;
	private Long matchDayId;
	private Long matchId;
	private Date matchTime;
	private League league;
	private Long hostId;
	private String hostName;
	private Long guestId;
	private String guestName;
	private EuroPl euroAvg;
	private ClubDatas baseData;
	private List<FinishedMatch> jiaoShou;
	private List<FinishedMatch> hostMatches;
	private List<FinishedMatch> guestMatches;
	private Map<Company, List<EuroPl>> euroPls;
	private List<AsiaPl> aoMen;
	private List<AsiaPl> daxiao;
	private List<AsiaPl> ysb;
	private Integer hostScore;
	private Integer guestScore;
	private boolean isSameCityOrNeutral = false;
	private int levelDiff = 0;
}
