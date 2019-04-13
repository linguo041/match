package com.roy.football.match.util;

import com.mysema.commons.lang.Pair;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.TeamLevel;

public class MatchStateUtil {
	
	/**
	 * what if the host win is supported?
	 *     host-guest differ on attack and defense
	 *      
	 * 
	 *                      base         				          state
	 * First condition:    host_level > guest_level               hotPoint > 0
	 * Second condition:   
	 * 
	 * 
	 * @param matchState
	 * @param clubMatrices
	 * @return
	 */
	public static boolean isBaseAndStateSupportHost (MatchState matchState, float pk, boolean isSameCityOrNeutral, League le) {
		LatestMatchMatrices host6Match = matchState.getHostState6();
		LatestMatchMatrices guest6Match = matchState.getGuestState6();
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		
		Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
				isSameCityOrNeutral, le, true);
		
		LatestMatchMatrices hostMatches = pair.getFirst();
		LatestMatchMatrices guestMatches = pair.getSecond();
		

		
		return false;
	}
	
	public static Pair<LatestMatchMatrices, LatestMatchMatrices> getComparedLatestMatchMatrices (
			MatchState matchState, boolean isDistinctHomeAway, League le, boolean considerAll) {
		LatestMatchMatrices hostMatches = matchState.getHostHome5();
		LatestMatchMatrices guestMatches = matchState.getGuestAway5();
		
		if (considerAll || !isDistinctHomeAway || le.isState() || hostMatches == null || guestMatches == null) {
			hostMatches = matchState.getHostState6();
			guestMatches = matchState.getGuestState6();
		}
		
		return Pair.of(hostMatches, guestMatches);
	}
	
	public static boolean divideHostGuest () {
		return true;
	}
	
	public static enum TeamDataLevel {
		Stronger, Strong, Normal, Weak, Weaker
	}
}
