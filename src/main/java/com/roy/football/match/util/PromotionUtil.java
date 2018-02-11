package com.roy.football.match.util;

import com.roy.football.match.OFN.MatchPromoter.MatchPull;

public class PromotionUtil {

	public static float getAvgWinDegreeByPk (float pk) {
		if (pk >= 1.75f) {
			return 10f;
		} else if (pk >= 1.5f) {
			return 8f;
		} else if (pk >= 1.25f) {
			return 7.5f;
		} else if (pk >= 1f) {
			return 7f;
		} else if (pk >= 0.75f) {
			return 6.5f;
		} else if (pk >= 0.5f) {
			return 6f;
		} else if (pk >= 0.25f) {
			return 5.5f;
		} else if (pk >= 0f) {
			return 5f;
		} else if (pk >= -0.25f) {
			return 4.5f;
		} else if (pk >= -0.5f) {
			return 4f;
		} else if (pk >= -0.75f) {
			return 3.5f;
		} else {
			return 3f;
		}
	}
	
	public static float getAvgDrawDegreeByPk (float pk) {
		if (pk >= 1.25f) {
			return 3f;
		} else if (pk >= 1f) {
			return 3.5f;
		} else if (pk >= 0.75f) {
			return 4f;
		} else if (pk >= 0.5f) {
			return 4.5f;
		} else if (pk >= 0.25f) {
			return 5f;
		} else if (pk >= 0f) {
			return 5f;
		} else if (pk >= -0.25f) {
			return 5f;
		} else if (pk >= -0.5f) {
			return 4.5f;
		} else if (pk >= -0.75f) {
			return 4f;
		} else if (pk >= -1f) {
			return 3.5f;
		} else {
			return 3f;
		}
	}
	
	public static float getAvgLoseDegreeByPk (float pk) {
		if (pk >= 1f) {
			return 2.5f;
		} else if (pk >= 0.75f) {
			return 3f;
		} else if (pk >= 0.5f) {
			return 3.5f;
		} else if (pk >= 0.25f) {
			return 4f;
		} else if (pk >= 0f) {
			return 4.5f;
		} else if (pk >= -0.25f) {
			return 5;
		} else if (pk >= -0.5f) {
			return 5.5f;
		} else if (pk >= -0.75f) {
			return 6;
		} else {
			return 6.5f;
		}
	}
	
	public static float getHotDiffRateByPredictAndPull (float pankou, float predictPk,float currentPk, float mainPk, MatchPull pull) {
		float diffWeight = Math.abs(pankou) > 1f ? 2f : (Math.abs(pankou) >= 0.75 ? 2.5f : 3f);
		float diff = (mainPk - predictPk) * 0.5f + (currentPk - predictPk) * 0.5f;
		
		float rate = diff * diffWeight - pull.getHPull() * 0.06f;

		return rate;
	}
	
	/*
	 * the diff usually is caused by the latest status, but there are some other factors may make diff,
	 *  e.g. 1) local debay may week the diff
	 *  	 2) team with frequent plays may change the diff, diff-x for host, diff+x for guest
	 *       3) the will may change the diff, team for grading may has more influence.
	 */
	public static float getDrawHotDiffRateByPredictAndPull (float pankou, float predictPk,float currentPk, float mainPk, MatchPull pull) {
		float diff = (mainPk - predictPk) * 0.5f + (currentPk - predictPk) * 0.5f;
		
		float rate = 0f;
		
		// e.g. diff = 0.2f, pull=6 
		if (pankou >= 1f) {
			rate = (diff * 2f - pull.getHPull() * 0.06f) * -1;
		} else if (pankou >= 0.5f) {
			rate = (diff * 2.5f - pull.getHPull() * 0.06f) * -1;
		} else if (pankou >= 0.25f) {
			rate = (diff * 3f - pull.getHPull() * 0.06f) * -1 * 0.6f; // week the affect
		} else if (pankou <= -1f) {
			rate = (diff * 2f - pull.getHPull() * 0.06f);
		} else if (pankou <= -0.5f) {
			rate = (diff * 2.5f - pull.getHPull() * 0.06f);
		} else if (pankou <= -0.25f) {
			rate = (diff * 3f - pull.getHPull() * 0.06f) * 0.6f; // week
		} else {
			rate = pull.getHPull() * -0.06f;
		}

		return rate;
	}
	
	public static float getWinLoseRateFromAomenEuro (float pankou, float aleWinDiff,
			float aleOriginWinDiff, float aaWinDiff, float aomenWinChange, boolean winRate) {
		float rate = 0f;
		float pk = winRate ? pankou : pankou * -1f;
		
		// e.g. diff = 0.2f, pull=6 
		if (pk >= 0.5f) {
			rate = aleWinDiff * (-4f) + aleOriginWinDiff * (-4f) + aaWinDiff * (-15f) + aomenWinChange * (-45) * 0.5f;
		} else if (pk >= 0.25f) {
			rate = aleWinDiff * (-2.5f) + aleOriginWinDiff * (-2.5f) + aaWinDiff * (-10f) + aomenWinChange * (-45) * 0.5f;
		} else if (pk <= -0.5f) {
			rate = aleWinDiff * (-2.5f) + aleOriginWinDiff * (-2.5f) + aaWinDiff * (-2.5f) + aomenWinChange * (-45) * 0.5f;
		} else if (pk <= -0.25f) {
			rate = aleWinDiff * (-2.5f) + aleOriginWinDiff * (-2.5f) + aaWinDiff * (-5f) + aomenWinChange * (-45) * 0.5f;
		} else {
			rate = aleWinDiff * (-2f) + aleOriginWinDiff * (-2f) + aaWinDiff * (-8f) + aomenWinChange * (-4) * 0.5f;
		}

		return rate;
	}
	
	public static float getDrawRateFromAomenEuro (float pk, boolean useAvg,
			float aoCurrentDraw, float aoOriginDraw,
			float aleDrawDiff, float aleOriginDrawDiff, float aaDrawDiff, float aomenDrawChange) {
		float rate = 0f;
		
		float avgDraw = 3.2f;
		
		if (Math.abs(pk) >= 1.5f) {
			avgDraw = 4.40f;
		} else if (Math.abs(pk) >= 1.25f) {
			avgDraw = 4.15f;
		} else if (Math.abs(pk) >= 1f) {
			avgDraw = 3.85f;
		} else if (Math.abs(pk) >= 0.75f) {
			avgDraw = 3.6f;
		} else if (Math.abs(pk) >= 0.5f) {
			avgDraw = 3.4f;
		} else {
			avgDraw = 3.2f;
		}
		
		rate += MatchUtil.getEuDiff(aoOriginDraw, avgDraw, false) * (-5f) + MatchUtil.getEuDiff(aoCurrentDraw, avgDraw, false) * (-5f);

		if (useAvg && pk != 0f) {
			rate = rate * 0.5f + (aleDrawDiff * (-5f) + aleOriginDrawDiff * (-7.5f)) * 0.5f;
		}
		
		rate += (aaDrawDiff+0.01f) * (-6f) + aomenDrawChange * (-45) * 0.5;
		
		return rate;
	}
}
