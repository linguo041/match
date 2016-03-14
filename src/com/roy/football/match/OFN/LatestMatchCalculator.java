package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class LatestMatchCalculator extends AbstractBaseDataCalculator implements Calculator<MatchState, OFNMatchData> {

	@Override
	public MatchState calucate(OFNMatchData matchData) {
		if (matchData != null) {
			MatchState matchState = new MatchState();
			
			calculateMatchMatrices(matchState, matchData.getHostMatches(), matchData.getHostId(), matchData.getMatchTime(), true);
			calculateMatchMatrices(matchState, matchData.getGuestMatches(), matchData.getGuestId(), matchData.getMatchTime(), false);
			matchState.setCalculatePk(getPankouByFinishedMatches(matchData));
			
			return matchState;
		}
		return null;
	}

	@Override
	public void calucate(MatchState Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private Float getPankouByFinishedMatches (OFNMatchData matchData) {
		Long hostId = matchData.getHostId();
		Long guestId = matchData.getGuestId();
		List<FinishedMatch> hMatches = matchData.getHostMatches();
		List<FinishedMatch> gMatches = matchData.getGuestMatches();

		int comparedMatches = 16;
		try {
			League le = League.getLeagueById(matchData.getLeagueId());
			if (le != null) {
				comparedMatches = (int)(le.getClubNum() * 0.8);
			}
		} catch (Exception e) {
			
		}
		
		List <Float> calPankous = new ArrayList<Float>();
		
		for (int i = 0; i < hMatches.size() && i < comparedMatches; i++) {
			Long opponentId = null;
			Float calPk = null;
			Float hPk = null;
			Float gPk = null;
			
			FinishedMatch hMatch = hMatches.get(i);
			
			try {
				hPk = Float.parseFloat(hMatch.getAsiaPanKou());
			} catch (Exception e) {
				continue;
			}

			// home
			if (hostId.equals(hMatch.getHostId())) {
				opponentId = hMatch.getGuestId();
				hPk = hPk - 0.25f;
			} else { // away
				opponentId = hMatch.getHostId();
				hPk = hPk * -1 + 0.25f;
			}
			
			for (int j = 0; j < gMatches.size() && j < comparedMatches; j++) {
				FinishedMatch gMatch = gMatches.get(j);
				
				try {
					gPk = Float.parseFloat(gMatch.getAsiaPanKou());
				} catch (Exception e) {
					continue;
				}
				
				if (guestId.equals(gMatch.getHostId()) && opponentId.equals(gMatch.getGuestId())) {
					gPk = gPk - 0.25f;
					calPk = hPk - gPk;
					calPankous.add(calPk);
				} else if (guestId.equals(gMatch.getGuestId()) && opponentId.equals(gMatch.getHostId())){
					gPk = gPk * -1 + 0.25f;
					calPk = hPk - gPk;
					calPankous.add(calPk);
				}
			}
		}
		
		if (calPankous.size() > 0) {
			Float allpk = 0f;

			for (Float pk : calPankous) {
				allpk += pk;
			}
			
			return allpk/calPankous.size() + 0.25f;
		}
		
		return null;
	}
	
	private void calculateMatchMatrices (MatchState matchState,
			List<FinishedMatch> matches, Long teamId, Date matchDate, boolean isHost) {
		if (matchState != null && matches != null && matches.size() > 0) {
			int winNum = 0;
			int drawNum = 0;
			int loseNum = 0;
			int allNum = 0;
			int winPkNum = 0;
			int drawPkNum = 0;
			int losePkNum = 0;
			int goals = 0;
			int misses = 0;
			
			for (int i = 0; i < matches.size(); i++) {
				FinishedMatch match = matches.get(i);
				
				// not friendly
				if (!match.getLeagueId().equals(League.Friendly.getLeagueId())) {
					if (MatchUtil.isMatchTooOld(match.getMatchTime(), matchDate, i)
							|| matchDate.getTime() < match.getMatchTime().getTime()) {
						break;
					}

					// home match
					if (teamId.equals(match.getHostId())) {
						if (match.getHscore() > match.getAscore()) {
							winNum ++;
						} else if (match.getHscore() == match.getAscore()) {
							drawNum ++;
						} else {
							loseNum ++;
						}

						goals += match.getHscore();
						misses += match.getAscore();
						
						if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
							winPkNum ++;
						} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
							drawPkNum ++;
						} else {
							losePkNum ++;
						}
					} else { // away match
						if (match.getAscore() > match.getHscore()) {
							winNum ++;
						} else if (match.getAscore() == match.getHscore()) {
							drawNum ++;
						} else {
							loseNum ++;
						}

						goals += match.getAscore();
						misses += match.getHscore();
						
						if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
							winPkNum ++;
						} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
							drawPkNum ++;
						} else {
							losePkNum ++;
						}
					}
					
					allNum ++;
				}
				
				// calculate the latest 6 matches
				if (allNum == 5) {
					if (isHost) {
						matchState.setHostState6(setMatchMatricesData(winNum, drawNum, loseNum, allNum, goals, misses, winPkNum, drawPkNum));
					} else {
						matchState.setGuestState6(setMatchMatricesData(winNum, drawNum, loseNum, allNum, goals, misses, winPkNum, drawPkNum));
					}
				}
				
				// calculate the latest 10 matches
				if (allNum == 9) {
					if (isHost) {
						matchState.setHostState10(setMatchMatricesData(winNum, drawNum, loseNum, allNum, goals, misses, winPkNum, drawPkNum));
					} else {
						matchState.setGuestState10(setMatchMatricesData(winNum, drawNum, loseNum, allNum, goals, misses, winPkNum, drawPkNum));
					}
					
					break;
				}
			}
		}
	}

	private LatestMatchMatrices setMatchMatricesData (int winNum,
			int drawNum, int loseNum, int allNum, int goals, int misses, int winPkNum, int drawPkNum) {
		LatestMatchMatrices matrices =  new LatestMatchMatrices();
		matrices.setMatchGoal((float)goals/allNum);
		matrices.setMatchMiss((float)misses/allNum);
		matrices.setWinRate((float)winNum/allNum);
		matrices.setWinDrawRate((float) (winNum + drawNum)/allNum);
		matrices.setWinPkRate((float) winPkNum / allNum);
		matrices.setWinDrawPkRate((float) (winPkNum + drawPkNum) / allNum);
		return matrices;
	}
}
