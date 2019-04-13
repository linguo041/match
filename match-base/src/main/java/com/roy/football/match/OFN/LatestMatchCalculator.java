package com.roy.football.match.OFN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;

import com.mysema.commons.lang.Pair;
import com.roy.football.match.OFN.response.ClubDatas;
import com.roy.football.match.OFN.response.ClubDatas.ClubData;
import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.service.HistoryMatchCalculationService;
import com.roy.football.match.util.MatchStateUtil;
import com.roy.football.match.util.MatchUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LatestMatchCalculator extends AbstractBaseDataCalculator implements Calculator<MatchState, OFNMatchData> {

	@Override
	public MatchState calucate(OFNMatchData matchData) {
		if (matchData == null) {
			return null;
		}

		MatchState matchState = new MatchState();

		League le = matchData.getLeague();
		
		Float originalPk = matchData.getOriginalPk();
		Float mainPk = matchData.getMainPk();

//		ClubDatas clubData = matchData.getBaseData();
//		if (clubData != null) {
//			ClubData hostClubData = clubData.getHostData();
//			ClubData guestClubData = clubData.getGuestData();
//		}

		calculateMatchMatrices(matchState, originalPk, mainPk, matchData.getHostMatches(),
				matchData.getHostId(), matchData.getMatchTime(), true, le);
		calculateMatchMatrices(matchState, originalPk, mainPk, matchData.getGuestMatches(),
				matchData.getGuestId(), matchData.getMatchTime(), false, le);
		matchState.setCalculatePk(getPankouByFinishedMatches(matchData, le));

		if (!calMatchStateIndex(matchState, matchData.isDistinctHomeAway(), matchData.getLevelDiff(), le)) {
			log.warn(String.format("No enough matches to calculate for matchId:%d, league:%s", matchData.getMatchId(), le));
		}
		
		calucateHotDiff(matchState);
		
//		log.info(matchState.toString());
		
		return matchState;
	}

	@Override
	public void calucate(MatchState Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private void calucateHotDiff (MatchState matchState) {
		float pointDiff = 0;

		if (matchState != null) {
			LatestMatchMatrices host6Match = matchState.getHostState6();
			LatestMatchMatrices guest6Match = matchState.getGuestState6();
			if (host6Match != null && guest6Match != null) {
				pointDiff = host6Match.getPoint() - guest6Match.getPoint();
			}
		}

		matchState.setHotPoint(pointDiff);
	}
	
	private boolean calMatchStateIndex (MatchState matchState, boolean isDistinctHomeAway, int levelDiff, League le) {		
		Pair<LatestMatchMatrices, LatestMatchMatrices> pair = MatchStateUtil.getComparedLatestMatchMatrices(matchState,
				isDistinctHomeAway, le, false);
		
		LatestMatchMatrices hostMatches = pair.getFirst();
		LatestMatchMatrices guestMatches = pair.getSecond();
		
		if (hostMatches == null || guestMatches == null) {
			return false;
		}

		// h_goal = (h_goal_avg - h_variance * (h_goal_avg - g_lose_avg) / h_goal_avg) *(4 - (h_level - g_level))/7
		//        + (g_lose_avg + g_variance * (h_goal_avg - g_lose_avg) / g_lose_avg) *(4 + (h_level - g_level))/7
		float hgoal =  (hostMatches.getMatchGoal() == 0 ? 0 : (hostMatches.getMatchGoal()
							 - hostMatches.getGVariation() * (hostMatches.getMatchGoal() - guestMatches.getMatchMiss())/Math.max(hostMatches.getMatchGoal(), guestMatches.getMatchMiss())
					   ) * (4 - levelDiff)/7)
					 + (guestMatches.getMatchMiss() == 0 ? 0 : (guestMatches.getMatchMiss()
							 + guestMatches.getMVariation() * (hostMatches.getMatchGoal() - guestMatches.getMatchMiss())/Math.max(hostMatches.getMatchGoal(), guestMatches.getMatchMiss())
					   ) * (4 + levelDiff)/7);
		// g_goal = (g_goal_avg - g_variance * (g_goal_avg - h_lose_avg) / g_goal_avg) *(4 + (h_level - g_level))/7
		//        + (h_lose_avg + h_variance * (g_goal_avg - h_lose_avg) / h_lose_avg) *(4 - (h_level - g_level))/7
		float ggoal = (guestMatches.getMatchGoal() == 0 ? 0 : (guestMatches.getMatchGoal()
							- guestMatches.getGVariation() * (guestMatches.getMatchGoal() - hostMatches.getMatchMiss())/Math.max(guestMatches.getMatchGoal(), hostMatches.getMatchMiss())
					  ) * (4 + levelDiff)/7)
					+ (hostMatches.getMatchMiss() == 0 ? 0 : (hostMatches.getMatchMiss()
							+ hostMatches.getMVariation() * (guestMatches.getMatchGoal() - hostMatches.getMatchMiss())/Math.max(guestMatches.getMatchGoal(), hostMatches.getMatchMiss())
					  ) * (4 - levelDiff)/7);

		float hvariation = hostMatches.getGVariation() * (4 - levelDiff)/7 + guestMatches.getMVariation() * (4 + levelDiff)/7;
		float gvariation = guestMatches.getGVariation() * (4 + levelDiff)/7 + hostMatches.getMVariation() * (4 - levelDiff)/7;

		matchState.setHostAttackToGuest(hgoal);
		matchState.setGuestAttackToHost(ggoal);
		matchState.setHostAttackVariationToGuest(hvariation);
		matchState.setGuestAttackVariationToHost(gvariation);
		matchState.setHostWinRt(hostMatches.getWinRate());
		matchState.setGuestWinRt(guestMatches.getWinRate());
		
		return true;
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
			hPk = hMatch.getAsiaPanKou() == null ? hMatch.getLastApk() : hMatch.getAsiaPanKou();
			
			if (hPk == null) {
//				log.warn(String.format("No pk data for match %d. [%s]",hMatch.getMatchId(), hMatch.toString()));
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
				gPk = gMatch.getAsiaPanKou() == null ? gMatch.getLastApk() : gMatch.getAsiaPanKou();
				
				if (gPk == null) {
//					log.warn(String.format("No pk data for match %d. [%s]",gMatch.getMatchId(), gMatch.toString()));
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
	
	private void calculateMatchMatrices (MatchState matchState, Float originalPk, Float mainPk,
			List<FinishedMatch> matches, Long teamId, Date matchDate, boolean isHost, League league) {
		if (matchState != null && matches != null && matches.size() > 0) {
			int index_H = 0;
			int index_A = 0;
			
			int winNum_H = 0;
			int drawNum_H = 0;
			int loseNum_H = 0;
			int winPkNum_H = 0;
			int drawPkNum_H = 0;
			int losePkNum_H = 0;
			double[] goals_H = new double[20];
			double[] misses_H = new double[20];
			float points_H = 0;
			
			int samePkWinNum_H = 0;
			int samePkDrawNum_H = 0;
			int samePkLoseNum_H = 0;
			int samePkWinPkNum_H = 0;
			int samePkDrawPkNum_H = 0;
			int samePkLosePkNum_H = 0;
			double[] samePkGoals_H = new double[20];
			double[] samePkMisses_H = new double[20];
			float samePkPoints_H = 0;
			int samePkIndex_H = 0;
			
			int winNum_A = 0;
			int drawNum_A = 0;
			int loseNum_A = 0;
			int winPkNum_A = 0;
			int drawPkNum_A = 0;
			int losePkNum_A = 0;
			double[] goals_A = new double[20];
			double[] misses_A = new double[20];
			float points_A = 0;
			
			int samePkWinNum_A = 0;
			int samePkDrawNum_A = 0;
			int samePkLoseNum_A = 0;
			int samePkWinPkNum_A = 0;
			int samePkDrawPkNum_A = 0;
			int samePkLosePkNum_A = 0;
			double[] samePkGoals_A = new double[20];
			double[] samePkMisses_A = new double[20];
			float samePkPoints_A = 0;
			int samePkIndex_A = 0;
			
			for (int i = 0; i < matches.size(); i++) {
				FinishedMatch match = matches.get(i);
				
				if (match == null
						|| MatchUtil.isMatchTooOld(match.getMatchTime(), matchDate, league.isState(), i)
						|| matchDate.getTime() <= match.getMatchTime().getTime()
						|| match.getAsiaPanKou() == null) {
					continue;
				}
				
				boolean samePkMatch = false;
				Float matchPk = match.getAsiaPanKou() == null
						? (match.getLastApk() == null ? null : match.getLastApk())
						: (match.getLastApk() == null ? match.getAsiaPanKou()
								: 0.5f * (match.getAsiaPanKou() + match.getLastApk()));
				
				if (Math.abs(matchPk - 0.5f * (originalPk + mainPk)) <= 0.2f) {
					samePkMatch = true;
				}
				
				// home match
				if (teamId.equals(match.getHostId())) {
					float point = 0;
					
					if (match.getHscore() > match.getAscore()) {
						winNum_H ++;
						point = 3;
						samePkWinNum_H = samePkMatch ? samePkWinNum_H + 1 : samePkWinNum_H;
					} else if (match.getHscore() == match.getAscore()) {
						drawNum_H ++;
						point = 1;
						samePkDrawNum_H = samePkMatch ? samePkDrawNum_H + 1 : samePkDrawNum_H;
					} else {
						loseNum_H ++;
						samePkLoseNum_H = samePkMatch ? samePkLoseNum_H + 1 : samePkLoseNum_H;
					}

					goals_H[index_H] = match.getHscore();
					misses_H[index_H] = match.getAscore();
					samePkGoals_H[samePkIndex_H] = samePkMatch ? match.getHscore() : samePkGoals_H[samePkIndex_H];
					samePkMisses_H[samePkIndex_H] = samePkMatch ? match.getAscore() : samePkMisses_H[samePkIndex_H];
					
					if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
						winPkNum_H ++;
						if (point != 3) {
							point += 0.5;
						}
						
						goals_H[index_H] *= 1.1;
						misses_H[index_H] *= 0.9;
					} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
						drawPkNum_H ++;
					} else {
						losePkNum_H ++;
						if (point != 0) {
							point -= 0.5;
						}
						
						goals_H[index_H] *= 0.9;
						misses_H[index_H] *= 1.1;
					}
					
					index_H++;
					samePkIndex_H = samePkMatch ? samePkIndex_H + 1 : samePkIndex_H;
					points_H += point;
				} else { // away match
					float point = 0;
					
					if (match.getAscore() > match.getHscore()) {
						winNum_A ++;
						point = 3;
						samePkWinNum_A = samePkMatch ? samePkWinNum_A + 1 : samePkWinNum_A;
					} else if (match.getAscore() == match.getHscore()) {
						drawNum_A ++;
						point = 1;
						samePkDrawNum_A = samePkMatch ? samePkDrawNum_A + 1 : samePkDrawNum_A;
					} else {
						loseNum_A ++;
						samePkLoseNum_A = samePkMatch ? samePkLoseNum_A + 1 : samePkLoseNum_A;
					}

					goals_A[index_A] = match.getAscore();
					misses_A[index_A] = match.getHscore();
					samePkGoals_A[samePkIndex_A] = samePkMatch ? match.getAscore() : samePkGoals_A[samePkIndex_A];
					samePkMisses_A[samePkIndex_A] = samePkMatch ? match.getHscore() : samePkMisses_A[samePkIndex_A];
					
					if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
						winPkNum_A ++;
						if (point != 3) {
							point += 0.5;
						}
						
						goals_A[index_A] *= 1.1;
						misses_A[index_A] *= 0.9;
					} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
						drawPkNum_A ++;
					} else {
						losePkNum_A ++;
						if (point != 0) {
							point -= 0.5;
						}
						
						goals_A[index_A] *= 0.9;
						misses_A[index_A] *= 1.1;
					}
					
					index_A++;
					points_A += point;
					samePkIndex_A = samePkMatch ? samePkIndex_A + 1 : samePkIndex_A;
				}

				// calculate the latest 6 matches
				if (index_H + index_A >= 4 && index_H + index_A <= 6) {
					LatestMatchMatrices latest6 = getMatchMatricesData(winNum_H + winNum_A,
							drawNum_H + drawNum_A, loseNum_H + loseNum_A, index_H + index_A,
							StatUtils.sum(goals_H) + StatUtils.sum(goals_A),
							StatUtils.sum(misses_H) + StatUtils.sum(misses_A),
							winPkNum_H + winPkNum_A, drawPkNum_H + drawPkNum_A, points_H + points_A,
							FastMath.sqrt(StatUtils.variance(ArrayUtils.addAll(goals_H, goals_A), 0, index_H + index_A)),
							FastMath.sqrt(StatUtils.variance(ArrayUtils.addAll(misses_H, misses_A), 0, index_H + index_A)));
					
					if (isHost) {
						matchState.setHostState6(latest6);
					} else {
						matchState.setGuestState6(latest6);
					}
				}
				
				if (isHost) {
					if (index_H >= 3 && index_H <= 5) {
						LatestMatchMatrices hostHome5 = getMatchMatricesData(winNum_H,
								drawNum_H, loseNum_H, index_H,
								StatUtils.sum(goals_H, 0, index_H),
								StatUtils.sum(misses_H, 0, index_H),
								winPkNum_H, drawPkNum_H, points_H,
								FastMath.sqrt(StatUtils.variance(goals_H, 0, index_H)),
								FastMath.sqrt(StatUtils.variance(misses_H, 0, index_H)));
						matchState.setHostHome5(hostHome5);
					}
					
					if (index_H > 5) {
						LatestMatchMatrices hostHome10 = getMatchMatricesData(winNum_H,
								drawNum_H, loseNum_H, index_H,
								StatUtils.sum(goals_H, 0, index_H),
								StatUtils.sum(misses_H, 0, index_H),
								winPkNum_H, drawPkNum_H, points_H,
								FastMath.sqrt(StatUtils.variance(goals_H, 0, index_H)),
								FastMath.sqrt(StatUtils.variance(misses_H, 0, index_H)));
						matchState.setHostState10(hostHome10);
					}
				} else {
					if (index_A >= 3 && index_A <= 5) {
						LatestMatchMatrices guestAway5 = getMatchMatricesData(winNum_A,
								drawNum_A, loseNum_A, index_A,
								StatUtils.sum(goals_A, 0, index_A),
								StatUtils.sum(misses_A, 0, index_A),
								winPkNum_A, drawPkNum_A, points_A,
								FastMath.sqrt(StatUtils.variance(goals_A, 0, index_A)),
								FastMath.sqrt(StatUtils.variance(misses_A, 0, index_A)));
						matchState.setGuestAway5(guestAway5);
					}
					
					if (index_A > 5) {
						LatestMatchMatrices guestAway10 = getMatchMatricesData(winNum_A,
								drawNum_A, loseNum_A, index_A,
								StatUtils.sum(goals_A, 0, index_A),
								StatUtils.sum(misses_A, 0, index_A),
								winPkNum_A, drawPkNum_A, points_A,
								FastMath.sqrt(StatUtils.variance(goals_A, 0, index_A)),
								FastMath.sqrt(StatUtils.variance(misses_A, 0, index_A)));
						matchState.setGuestState10(guestAway10);
					}
				}
				
//				log.info(match.toString());

				if (index_H >= 9 && index_A >= 9 || index_H + index_A >= 20) {
					if (samePkIndex_H >= 3) {
						LatestMatchMatrices hostSamePk = getMatchMatricesData(samePkWinNum_H,
								samePkDrawNum_H, samePkLoseNum_H, samePkIndex_H,
								StatUtils.sum(samePkGoals_H, 0, samePkIndex_H),
								StatUtils.sum(samePkMisses_H, 0, samePkIndex_H),
								0, 0, 0,
								FastMath.sqrt(StatUtils.variance(samePkGoals_H, 0, samePkIndex_H)),
								FastMath.sqrt(StatUtils.variance(samePkMisses_H, 0, samePkIndex_H)));
						matchState.setHostSamePk(hostSamePk);
					}
					
					if (samePkIndex_A >= 3) {
						LatestMatchMatrices guestSamePk = getMatchMatricesData(samePkWinNum_A,
								samePkDrawNum_A, samePkLoseNum_A, samePkIndex_A,
								StatUtils.sum(samePkGoals_A, 0, samePkIndex_A),
								StatUtils.sum(samePkMisses_A, 0, samePkIndex_A),
								0, 0, 0,
								FastMath.sqrt(StatUtils.variance(samePkGoals_A, 0, samePkIndex_A)),
								FastMath.sqrt(StatUtils.variance(samePkMisses_A, 0, samePkIndex_A)));
						matchState.setGuestSamePk(guestSamePk);
					}
					
					break;
				}
			}
		}
	}

	private LatestMatchMatrices getMatchMatricesData (int winNum,
			int drawNum, int loseNum, int allNum, double goals, double misses, int winPkNum, int drawPkNum, float points, double gVariation, double mVariation) {
		LatestMatchMatrices matrices =  new LatestMatchMatrices();
		matrices.setMatchGoal((float)goals/allNum);
		matrices.setMatchMiss((float)misses/allNum);
		matrices.setWinRate((float)winNum/allNum);
		matrices.setWinDrawRate((float) (winNum + drawNum)/allNum);
		matrices.setWinPkRate((float) winPkNum / allNum);
		matrices.setWinDrawPkRate((float) (winPkNum + drawPkNum) / allNum);
		matrices.setGVariation((float)gVariation);
		matrices.setMVariation((float)mVariation);
		matrices.setPoint(points);
		return matrices;
	}
	
	public static void main (String args []) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		double a[] = new double[10];
		
		
		a[0] = 0;
		a[1] = 0;
		a[2] = 0;
		a[3] = 5;
		a[4] = 1;
		
		for (int ii=0; ii<5; ii++) {
			stats.addValue(a[ii]);
		}
		
		System.out.println("mean: " + StatUtils.mean(a, 0, 5));
		System.out.println("sample variance: " + StatUtils.variance(a, 0, 5));
		System.out.println("sample deviation: " + FastMath.sqrt(StatUtils.variance(a, 0, 5)));
		System.out.println("variance: " + StatUtils.populationVariance(a, 0, 5));
		System.out.println("deviation: " + FastMath.sqrt(StatUtils.populationVariance(a, 0, 5)));
		System.out.println("mean: " + stats.getMean());
		System.out.println("sample variance: " + stats.getVariance());
		System.out.println("sample deviation: " + stats.getStandardDeviation());
		System.out.println("variance: " + stats.getPopulationVariance());
		
		double b[] = new double[]{1.25, 1.42};
		System.out.println("pv: " + FastMath.sqrt(StatUtils.populationVariance(b)));
		System.out.println("pv: " + StatUtils.mean(b));
		System.out.println("pv: " + StatUtils.geometricMean(b));
	}
}
