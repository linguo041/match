package com.roy.football.match.OFN.parser;

public class OFHKey {
	public static enum Match implements IShortKey {
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
			return shotKey;
		}

		public static Match shortKeyOf (String shortKey) {
			if (shortKey == null) {
				return null;
			}
			
			for (Match m : Match.values()) {
				if (m.shotKey.equalsIgnoreCase(shortKey)) {
					return m;
				}
			}
			
			return null;
		}
		
		private String shotKey;
	}
}
