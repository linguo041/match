package com.roy.football.match.sina;

import java.util.List;

import lombok.Data;

@Data
public class AicaiEuroResult {
	private EuropDetailOddsVoList result;
	
	@Data
	public static class EuropDetailOddsVoList {
		private List<AicaiEuro> europDetailOddsVoList;
	}
	
	@Data
	public static class AicaiEuro {
		private Long winOdds;
		private Long drowOdds;
		private Long loseOdds;
		private AicaiEuroDate createTime;
	}
	
	@Data
	public static class AicaiEuroDate {
		private Long time;
	}
}
