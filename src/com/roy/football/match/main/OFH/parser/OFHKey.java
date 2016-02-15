package com.roy.football.match.main.OFH.parser;

public class OFHKey {
	public enum Match implements IShortKey {
		MatchId("mid"),
		MatchTime("mtime"),
		HostName("homename"),
		GuestName("awayname"),
		HostId("hometid"),
		GuestId("awaytid"),
		BaseData("standing"),
		Recommend("recomm"),
		MatchSameAsia("pktong"),
		OldMatch("wars"),
		FutureMatch("future"),
		HostOldMatch("homeTeam"),
		GuestOldMatch("awayTeam");
		
		Match (String shortKey) {
			this.shotKey = shortKey;
		}

		@Override
		public String getShortKey() {
			// TODO Auto-generated method stub
			return shotKey;
		}
		
		private String shotKey;
	}
}
