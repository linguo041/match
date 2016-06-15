package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.ClubDatas.ClubData;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class LatestMatchCalculator extends AbstractBaseDataCalculator implements Calculator<MatchState, OFNMatchData> {

	@Override
	public MatchState calucate(OFNMatchData matchData) {
		if (matchData == null) {
			return null;
		}

		MatchState matchState = new MatchState();
		ClubDatas clubData = matchData.getBaseData();
		ClubData hostClubData = null;
		ClubData guestClubData = null;

		League le = League.getLeagueById(matchData.getLeagueId());

		if (clubData != null) {
			hostClubData = clubData.getHostData();
			guestClubData = clubData.getGuestData();
		}

		calculateMatchMatrices(matchState, matchData.getHostMatches(),
				matchData.getHostId(), matchData.getMatchTime(), hostClubData, true, le);
		calculateMatchMatrices(matchState, matchData.getGuestMatches(),
				matchData.getGuestId(), matchData.getMatchTime(), guestClubData, false, le);
		matchState.setCalculatePk(getPankouByFinishedMatches(matchData, le));

		calMatchStateIndex(matchState);
		return matchState;
	}

	@Override
	public void calucate(MatchState Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private void calMatchStateIndex (MatchState matchState) {
		LatestMatchMatrices hostMatches = matchState.getHostState6();
		LatestMatchMatrices guestMatches = matchState.getGuestState6();
		
		if (hostMatches == null) {
			return;
		}

		float hgoal = 0.6f * hostMatches.getMatchGoal() + 0.4f * guestMatches.getMatchMiss();
		float ggoal = 0.6f * guestMatches.getMatchGoal() + 0.4f * hostMatches.getMatchMiss();

		float hvariation = 0.6f * hostMatches.getgVariation() + 0.4f * guestMatches.getmVariation();
		float gvariation = 0.6f * guestMatches.getgVariation() + 0.4f * hostMatches.getmVariation();

		matchState.setHostAttackToGuest(hgoal);
		matchState.setGuestAttackToHost(ggoal);
		matchState.setHostAttackVariationToGuest(hvariation);
		matchState.setGuestAttackVariationToHost(gvariation);
	}
	
	private Float getPankouByFinishedMatches (OFNMatchData matchData, League le) {
		Long hostId = matchData.getHostId();
		Long guestId = matchData.getGuestId();
		List<FinishedMatch> hMatches = matchData.getHostMatches();
		List<FinishedMatch> gMatches = matchData.getGuestMatches();

		int comparedMatches = 16;
		if (le != null && le.getClubNum() > 0) {
			comparedMatches = (int)(le.getClubNum() * 0.8);
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
			
			String hPl = hMatch.getAsiaPanLu();
			if (MatchUtil.UNICODE_WIN.equalsIgnoreCase(hPl)) {
				hPk = hPk + 0.05f;
			} else if (MatchUtil.UNICODE_LOSE.equalsIgnoreCase(hPl)) {
				hPk = hPk - 0.05f;
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
					
					String gPl = gMatch.getAsiaPanLu();
					if (MatchUtil.UNICODE_WIN.equalsIgnoreCase(gPl)) {
						gPk = gPk + 0.05f;
					} else if (MatchUtil.UNICODE_LOSE.equalsIgnoreCase(gPl)) {
						gPk = gPk - 0.05f;
					}
					
					calPk = hPk - gPk;
					calPankous.add(calPk);
				} else if (guestId.equals(gMatch.getGuestId()) && opponentId.equals(gMatch.getHostId())){
					gPk = gPk * -1 + 0.25f;
					
					String gPl = gMatch.getAsiaPanLu();
					if (MatchUtil.UNICODE_WIN.equalsIgnoreCase(gPl)) {
						gPk = gPk + 0.05f;
					} else if (MatchUtil.UNICODE_LOSE.equalsIgnoreCase(gPl)) {
						gPk = gPk - 0.05f;
					}
					
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
			List<FinishedMatch> matches, Long teamId, Date matchDate, ClubData club, boolean isHost, League league) {
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
			float points = 0;

			boolean checkVariation = false;
			float goalPerMatch = 0;
			float missPerMatch = 0;
			float gVariation = 0;
			float mVariation = 0;
			
			if (club != null && club.getAllNum() != 0) {
				goalPerMatch = club.getAllGoal() / club.getAllNum();
				missPerMatch = club.getAllMiss() / club.getAllNum();
				checkVariation = true;
			}
			
			for (int i = 0; i < matches.size(); i++) {
				FinishedMatch match = matches.get(i);
				
				if (MatchUtil.isMatchTooOld(match.getMatchTime(), matchDate, i,
							league.isState() ? MatchUtil.STATE_LATEST_MIN_MATCH_DAY : MatchUtil.CLUB_LATEST_MIN_MATCH_DAY)
						|| matchDate.getTime() <= match.getMatchTime().getTime()) {
					continue;
				}
				
				float point = 0;

				// home match
				if (teamId.equals(match.getHostId())) {
					if (match.getHscore() > match.getAscore()) {
						winNum ++;
						point = 3;
					} else if (match.getHscore() == match.getAscore()) {
						drawNum ++;
						point = 1;
					} else {
						loseNum ++;
					}

					goals += match.getHscore();
					misses += match.getAscore();
					
					if (checkVariation) {
						gVariation += Math.abs(match.getHscore() - goalPerMatch);
						mVariation += Math.abs(match.getAscore() - missPerMatch);
					}
					
					if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
						winPkNum ++;
						if (point != 3) {
							point += 0.5;
						}
					} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
						drawPkNum ++;
					} else {
						losePkNum ++;
						if (point != 0) {
							point -= 0.5;
						}
					}
				} else { // away match
					if (match.getAscore() > match.getHscore()) {
						winNum ++;
						point = 3;
					} else if (match.getAscore() == match.getHscore()) {
						drawNum ++;
						point = 1;
					} else {
						loseNum ++;
					}

					goals += match.getAscore();
					misses += match.getHscore();
					
					if (checkVariation) {
						gVariation += Math.abs(match.getAscore() - goalPerMatch);
						mVariation += Math.abs(match.getHscore() - missPerMatch);
					}
					
					if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
						winPkNum ++;
						if (point != 3) {
							point += 0.5;
						}
					} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
						drawPkNum ++;
					} else {
						losePkNum ++;
						if (point != 0) {
							point -= 0.5;
						}
					}
				}
				
				allNum ++;
				points += point;

				// calculate the latest 6 matches
				if (allNum == 5) {
					if (isHost) {
						matchState.setHostState6(setMatchMatricesData(winNum,
								drawNum, loseNum, allNum, goals, misses,
								winPkNum, drawPkNum, points, gVariation,
								mVariation));
					} else {
						matchState.setGuestState6(setMatchMatricesData(winNum,
								drawNum, loseNum, allNum, goals, misses,
								winPkNum, drawPkNum, points, gVariation,
								mVariation));
					}
				}
				
				// calculate the latest 10 matches
				if (allNum == 9) {
					if (isHost) {
						matchState.setHostState10(setMatchMatricesData(winNum,
								drawNum, loseNum, allNum, goals, misses,
								winPkNum, drawPkNum, points, gVariation,
								mVariation));
					} else {
						matchState.setGuestState10(setMatchMatricesData(winNum,
								drawNum, loseNum, allNum, goals, misses,
								winPkNum, drawPkNum, points, gVariation,
								mVariation));
					}
					
					break;
				}
			}
		}
	}

	private LatestMatchMatrices setMatchMatricesData (int winNum,
			int drawNum, int loseNum, int allNum, int goals, int misses, int winPkNum, int drawPkNum, float points, float gVariation, float mVariation) {
		LatestMatchMatrices matrices =  new LatestMatchMatrices();
		matrices.setMatchGoal((float)goals/allNum);
		matrices.setMatchMiss((float)misses/allNum);
		matrices.setWinRate((float)winNum/allNum);
		matrices.setWinDrawRate((float) (winNum + drawNum)/allNum);
		matrices.setWinPkRate((float) winPkNum / allNum);
		matrices.setWinDrawPkRate((float) (winPkNum + drawPkNum) / allNum);
		matrices.setgVariation(gVariation / allNum);
		matrices.setmVariation(mVariation / allNum);
		matrices.setPoint(points);
		return matrices;
	}
}
