package com.roy.football.match.OFN;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.MatchPromoter.MatchPull;
import com.roy.football.match.OFN.MatchPromoter.MatchRank;
import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.OFNKillPromoteResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.OFN.statics.matrices.PromoteMatrics;
import com.roy.football.match.OFN.statics.matrices.PromoteMatrics.PromoteRatio;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchContinent;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchStateUtil;
import com.roy.football.match.util.MatchUtil;
import com.roy.football.match.util.PanKouUtil;
import com.roy.football.match.util.PromotionUtil;
import com.roy.football.match.util.PanKouUtil.PKDirection;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchPromoter {
	private static float FACTOR_H = 1.0f;
	private static float FACTOR_G = 1.0f;
	
	@Autowired
	private EuroAnalyzer euroAnalyzer;

	public OFNKillPromoteResult promote (OFNCalculateResult calResult) {
		OFNKillPromoteResult kpRes = new OFNKillPromoteResult();
		
		rankByBase(kpRes.getRank(), kpRes.getPull(), calResult.getClubMatrices());
		rankByLatest(kpRes.getRank(), kpRes.getPull(), calResult.getMatchState());
		promote(kpRes, calResult.getPkMatrices(),
				calResult.getEuroMatrices(), calResult.getPredictPanKou(), calResult.getLeague());
		
		return kpRes;
	}
	
	private void rankByBase (MatchRank rank, MatchPull pull, ClubMatrices clubMatrices) {
		// no enough match to calculate, give up ranking
		if (clubMatrices == null || clubMatrices.getHostAllMatrix() == null
				|| clubMatrices.getHostAllMatrix().getNum() <= 5) {
			return;
		}
		
		float hostAttGuestDefComp = clubMatrices.getHostAttGuestDefInx();
		float guestAttHostDefComp = clubMatrices.getGuestAttHostDefInx();
		ClubMatrix hostMatrix = clubMatrices.getHostAllMatrix();
		ClubMatrix guestMatrix = clubMatrices.getGuestAllMatrix();
		boolean divideHostGuest = MatchStateUtil.divideHostGuest();
		
		if (divideHostGuest) {
			hostMatrix = clubMatrices.getHostHomeMatrix();
			guestMatrix = clubMatrices.getGuestAwayMatrix();
		}
		
		// The factors calculated based on the history (TODO - split the factors per league or even per team)
		float rawDiff = FACTOR_H * hostAttGuestDefComp - FACTOR_G * guestAttHostDefComp;
		
		// degree = diff / 0.125; so 4 degree means half goal, and 8 degree means one goal
		// The max degree calculated by diff is 16, and if the possibilities of win-draw-lose equal, split 16 into 3 parts,
		// which turns to be 5-5-5, but the related diff is nearly 0, so how to calculate 0 to relate to 5?
		// Here is the thought: we add 5 more to the win degree, and make the total 21, then split the rest 15 according to the diff.
		int wDegree = 0;
		int dDegree = 0;
		int lDegree = 0;
		int restDegree = 32;
		int adjustDegree = 0;
		
		// adjust by home win rate for host and way win rate for guest
		float diff = calculateWinDegree(hostMatrix, guestMatrix, hostAttGuestDefComp, guestAttHostDefComp);
		
		// if diff is small, but little more than 3.125, then the adjust degree is 3, which adds too many, so need adjust.
//		if (diff >= 0.4375 && diff <= 0.46275) {
//			diff -= 0.03125f;
//		} else if (diff >= 0.3125 && diff <= 0.34375) {
//			diff -= 0.03125f;
//		} else if (diff <= -0.4375 && diff >= -0.46275) {
//			diff += 0.03125f;
//		} else if (diff <= -0.3125 && diff >= -0.34375) {
//			diff += 0.03125f;
//		}
		
		adjustDegree += Math.abs(Math.round(16 * diff));
		
		if (diff >= 0 ) {
			wDegree += 10 + adjustDegree;
			restDegree -= wDegree;
			dDegree = adjustDrawDegreeReferToDrawRate(rawDiff,  restDegree, hostMatrix, guestMatrix, hostAttGuestDefComp, guestAttHostDefComp);
			lDegree = restDegree - dDegree;
		} else {
			lDegree += 10 + adjustDegree;
			restDegree -= lDegree;
			dDegree = adjustDrawDegreeReferToDrawRate(rawDiff,  restDegree, hostMatrix, guestMatrix, hostAttGuestDefComp, guestAttHostDefComp);
			wDegree = restDegree - dDegree;
		}
		
		rank.setWRank(wDegree);
		rank.setDRank(dDegree);
		rank.setLRank(lDegree);
	}

	private void rankByLatest (MatchRank rank, MatchPull pull, MatchState matchState) {
		if (matchState == null || matchState.getHostAttackToGuest() == null) {
			return;
		}
		
		Float latestHostAttack = matchState.getHostAttackToGuest();
		Float latestGuestAttack = matchState.getGuestAttackToHost();
		Float hotPoint = matchState.getHotPoint();

		LatestMatchMatrices hostMatches = matchState.getHostState6();
		LatestMatchMatrices guestMatches = matchState.getGuestState6();
//		boolean divideHostGuest = MatchStateUtil.divideHostGuest();
//		
//		if (divideHostGuest) {
//			hostMatches = matchState.getHostHome5();
//			guestMatches = matchState.getGuestAway5();
//		}
		
		// adjust base degree based on latest status
		int wDegree = rank.getWRank();
		int dDegree = rank.getDRank();
		int lDegree = rank.getLRank();
		
		// by goal
		float diff = FACTOR_H * latestHostAttack - FACTOR_G * latestGuestAttack;
		int degree = Math.round(16 * diff);
		if (degree >= 16) {
			// latest is better than base
			if (wDegree <= 10) {
				wDegree += 2;
				lDegree -= 2;
			} else if (wDegree <= 14) {
				wDegree ++;
				lDegree --;
			}
		} else if (degree >= 8) {
			// latest is worse than base
			if (wDegree >= 20) {
				wDegree -= 2;
				dDegree ++;
				lDegree ++;
			} else if (wDegree >= 16) {
				wDegree --;
				dDegree ++;
			} else if (wDegree >= 12) {
				
			} else if (wDegree >= 8) {
				wDegree ++;
				lDegree --;
			} else {
				wDegree += 2;
				dDegree --;
				lDegree --;
			}
		} else if (degree >= -8) {
			// latest is worse than base
			if (wDegree >= 16) {
				wDegree -= 2;
				dDegree ++;
				lDegree ++;
			} else if (wDegree >= 12 && degree <= 4) {
				wDegree --;
				lDegree ++;
			}
			// latest is good than base
			if (lDegree >= 16) {
				lDegree -=2;
				dDegree ++;
				wDegree ++;	
			} else if (lDegree >= 12 && degree >= -4) {
				lDegree --;
				wDegree ++;
			}
		} else if (degree >= -16){
			// latest is worse than base
			if (lDegree >= 20) {
				lDegree -= 2;
				dDegree ++;
				wDegree ++;
			} else if (lDegree >= 16) {
				lDegree --;
				dDegree ++;
			} else if (lDegree >= 12) {
				
			} else if (lDegree >= 8) {
				lDegree ++;
				wDegree --;
			} else {
				lDegree +=2;
				dDegree --;
				wDegree --;
			}
		} else {
			if (lDegree <= 10) {
				lDegree +=2;
				dDegree --;
				wDegree --;
			} else if (lDegree <= 14) {
				lDegree ++;
				wDegree --;
			}
		}
		
		// by hotpoint
		if (hotPoint >= 6) {
			// latest is better than base
			if (wDegree <= 10) {
				wDegree += 2;
				dDegree --;
				lDegree --;
			} else {
				wDegree ++;
				lDegree --;
			}
		} else if (hotPoint >= 3) {
			// latest is worse than base
			if (wDegree >= 18) {
				wDegree -= 2;
				dDegree ++;
				lDegree ++;
			} else if (wDegree >= 10) {
				
			} else {
				wDegree += 2;
				dDegree --;
				lDegree --;
			}
		} else if (hotPoint > -3) {
			// latest is worse than base
			if (wDegree >= 18) {
				wDegree -= 2;
				dDegree ++;
				lDegree ++;
			} else if (wDegree > 14) {
				wDegree --;
				lDegree ++;
			}
			// latest is good than base
			if (lDegree >= 18) {
				lDegree -= 2;
				dDegree ++;
				wDegree ++;
			} else if (lDegree > 14) {
				lDegree --;
				wDegree ++;
			}
		} else if (hotPoint >= -6){
			// latest is worse than base
			if (lDegree >= 18) {
				lDegree -= 2;
				dDegree ++;
				wDegree ++;
			} else if (lDegree >= 10) {
				
			} else {
				wDegree -= 2;
				dDegree ++;
				lDegree ++;
			}
		} else {
			if (lDegree <= 10) {
				lDegree += 2;
				dDegree --;
				wDegree --;
			} else {
				lDegree ++;
				wDegree --;
			}
		}

		rank.setWRank(wDegree);
		rank.setDRank(dDegree);
		rank.setLRank(lDegree);
		
		float hPull = 0f;
		float gPull = 0f;
		
		if (hotPoint > 0) {
			hPull = hotPoint;
		} else {
			gPull = Math.abs(hotPoint);
		}
		
		pullOfHeavyMatch(pull, matchState);
		
		pull.setHPull(pull.getHPull() + hPull);
		pull.setGPull(pull.getGPull() + gPull);
	}
	
	private void pullOfHeavyMatch (MatchPull pull, MatchState matchState) {
		
	}
	
	private void rankByJiaoShou (MatchRank rank, MatchPull pull) {
		
	}

	private void promote (OFNKillPromoteResult killPromoteResult, PankouMatrices pkMatrices,
			EuroMatrices euroMatrices, Float predictPk, League le) {
		if (pkMatrices == null) {
			return;
		}
		
		MatchRank rank = killPromoteResult.getRank();
		MatchPull pull = killPromoteResult.getPull();
		PromoteMatrics promoteMatrics = killPromoteResult.getPromoteMatrics();
		Set<ResultGroup> promote = killPromoteResult.getPromoteByBase();

		// Euro pl
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		EuroPl euroAvg = euroMatrices.getCurrEuroAvg();
		EuroMatrix aomen = companyEus.get(Company.Aomen);
		EuroMatrix jincai = companyEus.get(Company.Jincai);
		EuroMatrix will = companyEus.get(Company.William);
		float jcWinChange = jincai.getWinChange();
		float jcDrawChange = jincai.getDrawChange();
		float jcLoseChange = jincai.getLoseChange();
		float aomenWinChange = aomen.getWinChange();
		float aomenDrawChange = aomen.getDrawChange();
		float aomenLoseChange = aomen.getLoseChange();
		float willWinChange = will.getWinChange();
		float willDrawChange = will.getDrawChange();
		float willLoseChange = will.getLoseChange();
		
		float jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		float aaWinDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float aaDrawDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float aaLoseDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		float waWinDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
		float waDrawDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
		float waLoseDiff = MatchUtil.getEuDiff(will.getCurrentEuro().getELose(), euroAvg.getELose(), false);
		
		// pk
		AsiaPl origin = pkMatrices.getOriginPk();
		AsiaPl main = pkMatrices.getMainPk();
		AsiaPl current = pkMatrices.getCurrentPk();
		
		killByEuro(killPromoteResult.getKillByPl(), euroMatrices, pkMatrices, le);
		killPkPlUnmatchChange(current, aomen.getCurrentEuro(), le, killPromoteResult.getKillByPlPkUnmatch());
		
		if (predictPk == null) {
			return;
		}
		
		float originPk = MatchUtil.getCalculatedPk(origin);
		float mainPk = MatchUtil.getCalculatedPk(main);
		float currentPk = MatchUtil.getCalculatedPk(current);
		float pmPkDiff = predictPk - mainPk;
		float pcPkDiff = predictPk - currentPk;
		PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);
		boolean isAomenMajor = EuroUtil.isAomenTheMajor(le) || le.getContinent() == MatchContinent.Asia;
		Float upChange = pkMatrices.getHwinChangeRate();
		Float downChange = pkMatrices.getAwinChangeRate();
		ResultGroup firstOption = null;
		ResultGroup secondOption = null;
		
		killByPull(predictPk, current.getPanKou(), currentPk, mainPk, pull, killPromoteResult.getKillByPull());
		killByPk(killPromoteResult.getKillByPk(), rank, pull, pkMatrices, predictPk);
		
		EuroPl leAvg = euroAnalyzer.getLeagueAvgPl(current.getPanKou(), Company.Aomen, le);
		EuroPl leOriginAvg = euroAnalyzer.getLeagueAvgPl(origin.getPanKou(), Company.Aomen, le);
		float aleWinDiff = 0f;
		float aleDrawDiff = 0f;
		float aleLoseDiff = 0f;
		// the original pl is hard to compare, so don't use it 
		float aleOriginWinDiff = 0f;
		float aleOriginDrawDiff = 0f;
		float aleOriginLoseDiff = 0f;
		if (leAvg != null) {
			float pkBalance = current.getPanKou() - currentPk; // 0.75 - 0.9 = -0.15; -0.75 - (-0.9) = 0.15
			float pkOriginBalance = origin.getPanKou() - originPk;
			float balanceWinWeight = current.getPanKou() >= 0f ? 0.85f : 1.5f;
			float balanceDrawWeight = Math.abs(current.getPanKou()) <= 0.5f ? 0.8f : 1f;
			float balanceLoseWeight = current.getPanKou() <= 0f ? 0.85f : 1.5f;

			aleWinDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEWin(),
					leAvg.getEWin() + pkBalance * balanceWinWeight, false); // 1.4, 1.46 + (-0.15)
			aleDrawDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEDraw(),
					leAvg.getEDraw() - (current.getPanKou() > 0 ? pkBalance : pkBalance * -1) * balanceDrawWeight, false); // 3.8, 3.6 - (-0.15)
			aleLoseDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getELose(),
					leAvg.getELose() - pkBalance * balanceLoseWeight, false);
			
			aleOriginWinDiff = MatchUtil.getEuDiff(aomen.getOriginEuro().getEWin(),
					leOriginAvg.getEWin() + pkOriginBalance * balanceWinWeight, false);
			aleOriginDrawDiff = MatchUtil.getEuDiff(aomen.getOriginEuro().getEDraw(),
					leOriginAvg.getEDraw() - (current.getPanKou() > 0 ? pkOriginBalance : pkOriginBalance * -1) * balanceDrawWeight, false);
			aleOriginLoseDiff = MatchUtil.getEuDiff(aomen.getOriginEuro().getELose(),
					leOriginAvg.getELose() - pkOriginBalance * balanceLoseWeight, false);
			
			aomen.setLeAvgEuro(leAvg);
		}
		
		float winAdjByPull = adjustEuroDiffReferPull(pull);
		
		PromoteRatio winRatio = new PromoteRatio();
		winRatio.setRBaseDegree( (rank.getWRank() - PromotionUtil.getAvgWinDegreeByPk(current.getPanKou())) * 0.15f );
//		winRatio.setRPullNPredict( PromotionUtil.getHotDiffRateByPredictAndPull(current.getPanKou(), predictPk, currentPk, mainPk, pull));
		winRatio.setRAomen(PromotionUtil.getWinLoseRateFromAomenEuro(current.getPanKou(),
				aleWinDiff, aleOriginWinDiff, aaWinDiff, aomenWinChange, true));
		winRatio.setRJincai( jaWinDiff * (-3.5f) + jcWinChange * (-15));
		winRatio.setRWilliam( waWinDiff * (-5));
		
		PromoteRatio drawRatio = new PromoteRatio();
		drawRatio.setRBaseDegree( (rank.getDRank() - PromotionUtil.getAvgDrawDegreeByPk(current.getPanKou())) * 0.175f);
//		drawRatio.setRPullNPredict(PromotionUtil.getDrawHotDiffRateByPredictAndPull(current.getPanKou(), predictPk, currentPk, mainPk, pull));
		drawRatio.setRAomen( PromotionUtil.getDrawRateFromAomenEuro(current.getPanKou(), leAvg!=null, 
				aomen.getCurrentEuro().getEDraw(), aomen.getOriginEuro().getEDraw(), aleDrawDiff, aleOriginDrawDiff, aaDrawDiff, aomenDrawChange));
		drawRatio.setRJincai( jaDrawDiff * (-5) + jcDrawChange * (-15));
		drawRatio.setRWilliam( waDrawDiff * (-6));
		
		PromoteRatio loseRatio = new PromoteRatio();
		loseRatio.setRBaseDegree( (rank.getLRank() - PromotionUtil.getAvgLoseDegreeByPk(current.getPanKou())) * 0.15f );
//		loseRatio.setRPullNPredict( -1f * PromotionUtil.getHotDiffRateByPredictAndPull(current.getPanKou(), predictPk, currentPk, mainPk, pull) );
		loseRatio.setRAomen(PromotionUtil.getWinLoseRateFromAomenEuro(current.getPanKou(),
				aaLoseDiff, aleOriginLoseDiff, aaLoseDiff, aomenLoseChange, false));
		loseRatio.setRJincai( jaLoseDiff * (-3.5f) + jcLoseChange * (-15) );
		loseRatio.setRWilliam( waLoseDiff * (-5));
		
		promoteMatrics.setWinRatio(winRatio);
		promoteMatrics.setDrawRatio(drawRatio);
		promoteMatrics.setLoseRatio(loseRatio);
		
		if (current.getPanKou() >= 1) {
			if (isAomenMajor) {
				if (rank.getWRank() >= 14
						&& (pmPkDiff <= 0.35f && pcPkDiff <= 0.35f)
						&& (pkDirection.ordinal() > PKDirection.Downer.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.021f && aomenWinChange < 0.019f
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.015f
						&& aleWinDiff <= 0.025f) {
					if (aaWinDiff < -0.015f || jaWinDiff <= -0.025f) {
						// if pk is 1, then pk value should be low
						if (current.getPanKou() > 1) {
							firstOption = ResultGroup.Three;
						} else if (upChange <= 0){
							firstOption = ResultGroup.Three;
						}
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= 0.005f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f // jc draw diff is always high 
						&& aleDrawDiff <= 0.03f
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (jaDrawDiff <= -0.001f
								&& pkDirection.ordinal() < PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < 0.001f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.03f && jcLoseChange < 0.001f
						&& aleLoseDiff <= 0.01f
						) {
					if (aaLoseDiff < -0.025f || jaLoseDiff < -0.025f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (jaWinDiff > -0.05f
								&& pkDirection.ordinal() < PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if (rank.getWRank() >= 14
						&& (pmPkDiff <= 0.35f && pcPkDiff <= 0.35f)
						&& (pkDirection.ordinal() > PKDirection.Downer.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.021f && aomenWinChange < 0.011f
						&& waWinDiff < 0.04f
						&& jaWinDiff <= -0.04f
						&& aleWinDiff <= 0.025f) {
					if (aaWinDiff < -0.025f || jaWinDiff <= -0.05f) {
						// if pk is 1, then pk value should be low
						if (current.getPanKou() > 1) {
							firstOption = ResultGroup.Three;
						} else if (upChange <= 0){
							firstOption = ResultGroup.Three;
						}
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaDrawDiff <= -0.011f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.001f
						&& aleDrawDiff <= 0.03f
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (jaDrawDiff <= -0.001f
								&& pkDirection.ordinal() < PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.03f && aomenLoseChange < 0.011f
//						&& jaLoseDiff < 0.03f
						&& jcLoseChange < 0.005f
						&& aleLoseDiff <= 0.01f
						) {
					if (aaLoseDiff < -0.05f || jaLoseDiff < -0.05f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (jaWinDiff > -0.05f
								&& pkDirection.ordinal() < PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0.4) {
			if (isAomenMajor) {
				if ( (current.getPanKou() >= 0.75 && rank.getWRank() >= 12 || rank.getWRank() >= 10)
						&& upChange <= 0.065f
						&& (pmPkDiff <= 0.33f && pcPkDiff <= 0.33f)
						&& (pkDirection.ordinal() > PKDirection.Down.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.021f && aomenWinChange < 0.011f
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.02f
						&& aleWinDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaWinDiff < -0.015f || jaWinDiff <= -0.035f || rank.getWRank() >= 14)
							&& winNegatives(jaWinDiff, aaWinDiff, upChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Three;
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& (aaDrawDiff <= 0.011f && aomenDrawChange < 0.019f
								|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.31f)
						&& jaDrawDiff < 0.035f && jcDrawChange < 0.011f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.021f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.021f || jincai.getCurrentEuro().getEDraw() <= 3.20f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (rank.getDRank() >= 10
								&& aomen.getOriginEuro().getEDraw() <= (current.getPanKou() >= 0.75 ? 3.60f : 3.40f)
								&& (aaWinDiff > -0.015f || jaWinDiff > -0.045f || jcWinChange >= 0.015f)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.011f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.03f
						&& jcLoseChange < 0.005f
						&& aleLoseDiff <= 0.015f
						) {
					if (aaLoseDiff < -0.03f || jaLoseDiff < -0.05f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& rank.getLRank() >= 10
								&& (aaWinDiff > -0.015f || jaWinDiff > -0.045f || jcWinChange >= 0.015f)) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if ((current.getPanKou() >= 0.75 && rank.getWRank() >= 12 || rank.getWRank() >= 10)
						&& upChange <= 0.065f
						&& (pmPkDiff < 0.35f && pcPkDiff < 0.35f)
						&& (pkDirection.ordinal() > PKDirection.Down.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.015f && aomenWinChange < 0.015f
						&& waWinDiff < 0.04f
						&& jaWinDiff <= -0.04f
						&& aleWinDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaWinDiff < -0.02f || jaWinDiff <= -0.035f + winAdjByPull || rank.getWRank() >= 14)
							&& winNegatives(jaWinDiff, aaWinDiff, upChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Three;
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& (aaDrawDiff <= 0.011f && aomenDrawChange < 0.019f
							|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.31f)
						&& jaDrawDiff <= 0.015f - 2 * winAdjByPull && jcDrawChange < 0.011f
						&& waDrawDiff <= 0.05f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.03f || jincai.getCurrentEuro().getEDraw() <= 3.30f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (rank.getDRank() >= 10
								&& aomen.getOriginEuro().getEDraw() <= (current.getPanKou() >= 0.75 ? 3.60f : 3.40f)
								&& (aaWinDiff > -0.019f || jaWinDiff > -0.045f || jcWinChange >= 0.015f)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() < PKDirection.Uper.ordinal()
						&& aaLoseDiff < -0.021f && aomenLoseChange < 0.011f
//						&& jaLoseDiff < 0.015f - 2 * winAdjByPull 
						&& jcLoseChange < 0.005f
						&& aleLoseDiff <= 0.01f
						) {
					if (aaLoseDiff < -0.045f || jaLoseDiff < -0.05f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								&& rank.getLRank() >= 10
								&& (aaWinDiff > -0.019f || jaWinDiff > -0.045f || jcWinChange >= 0.015f)) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0.25) {
			if (isAomenMajor) {
				if (rank.getWRank() >= 10
						&& upChange <= 0.065f
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.021f && aomenWinChange < 0.011f
						&& waWinDiff <= 0.03f
						&& jaWinDiff <= -0.02f
						&& aleWinDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaWinDiff < -0.015f || jaWinDiff <= -0.02f + winAdjByPull || rank.getWRank() >= 14)
							&& winNegatives(jaWinDiff, aaWinDiff, upChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Three;
					}
				}
				if (rank.getDRank() >= 8
						&& (aaDrawDiff < 0.011f && aomenDrawChange < 0.019f
								|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.21f)
						&& jaDrawDiff <= 0.025f && jcDrawChange < 0.005f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.021f || jincai.getCurrentEuro().getEDraw() <= 3.20f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (rank.getDRank() >= 10
								&& aomen.getOriginEuro().getEDraw() <= 3.30f
								&& (aaDrawDiff <= -0.001 && jaDrawDiff <= -0.01)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() <= PKDirection.Middle.ordinal()
						&& aaLoseDiff < 0.005f && aomenLoseChange < 0.011f
						&& jaLoseDiff < 0.025f && jcLoseChange < 0.005f
						&& aleLoseDiff <= 0.03f
						&& rank.getDRank() <= 11
						) {
					if (aaLoseDiff < -0.03f || jaLoseDiff < -0.035f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (rank.getLRank() >= 10
								&& (aaWinDiff > -0.015f || jaWinDiff > -0.03f || jcWinChange >= 0.01f)) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			} else {
				if (rank.getWRank() >= 10
						&& upChange <= 0.065f
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& (pkDirection.ordinal() > PKDirection.Down.ordinal()
								|| pkDirection.ordinal() == PKDirection.Downer.ordinal() && upChange <= 0.04f)
						&& aaWinDiff < 0.011f && aomenWinChange < 0.011f
						&& waWinDiff <= 0.035f
						&& jaWinDiff <= -0.025f
						&& aleWinDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaWinDiff < -0.025f || jaWinDiff <= -0.045f + winAdjByPull
							|| rank.getWRank() >= 14
							|| pkDirection.ordinal() > PKDirection.Middle.ordinal())
							&& winNegatives(jaWinDiff, aaWinDiff, upChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Three;
					}
				}
				if (rank.getDRank() >= 8
							&& (aaDrawDiff <= -0.001f && aomenDrawChange < 0.019f
								|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.21f)
							&& jaDrawDiff <= 0.01f - 2 * winAdjByPull && jcDrawChange < 0.005f
							&& waDrawDiff <= 0.05f
							&& aleDrawDiff <= 0.03f
							&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.03f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.03f || jincai.getCurrentEuro().getEDraw() <= 3.20f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (aomen.getOriginEuro().getEDraw() <= 3.30f
								&& rank.getDRank() >= 10
								&& (aaDrawDiff <= -0.005 && jaDrawDiff <= -0.015)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getLRank() >= 8
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& pkDirection.ordinal() <= PKDirection.Middle.ordinal()
						&& aaLoseDiff < -0.011f && aomenLoseChange < 0.011f
//						&& jaLoseDiff < 0.005f -2 * winAdjByPull
						&& jcLoseChange < 0.005f
						&& aleLoseDiff <= 0.035f
						&& rank.getDRank() <= 11
						) {
					if ((aaLoseDiff < -0.03f || jaLoseDiff < -0.035f)) {
						if (firstOption != null) {
							secondOption = ResultGroup.Zero;
						} else if (rank.getLRank() >= 10
								&& (aaWinDiff > -0.015f || jaWinDiff > -0.03f || jcWinChange >= 0.01f)) {
							firstOption = ResultGroup.Zero;
						}
					}
				}
			}
		} else if (current.getPanKou() >= 0) {
			if (aaWinDiff < -0.03f && aomenWinChange < 0.011f
					&& jaWinDiff < -0.035f + winAdjByPull && jcWinChange < 0.011f
					&& waWinDiff <= 0.035f
					&& rank.getWRank() > 8
					) {
				if (rank.getWRank() < 14) {
					firstOption = ResultGroup.Three;
				}
			}
			if (aaLoseDiff < -0.03f && aomenLoseChange < 0.011f
					&& jaLoseDiff < -0.035f - winAdjByPull && jcLoseChange < 0.011f
					&& waLoseDiff <= 0.035f
					&& rank.getLRank() > 7
					) {
				if (rank.getLRank() < 14) {
					if (firstOption != null) {
						secondOption = ResultGroup.Zero;
					} else {
						firstOption = ResultGroup.Zero;
					}
				}
			}
			if (rank.getDRank() >= 10
					&& aaDrawDiff <= 0.035f && aomenDrawChange < 0.011f
					&& jaDrawDiff <= -0.001f && jcDrawChange < 0.005f
					&& waDrawDiff <= 0.035f
					&& aleDrawDiff <= 0.05f
					&& Math.abs(aaLoseDiff - aaWinDiff) < 0.022
					&& (aaLoseDiff > -0.04 || aaWinDiff > -0.04)
					) {
				if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
						|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f
						|| rank.getDRank() >= 12) {
					if (firstOption != null) {
						secondOption = ResultGroup.One;
					} else if (aomen.getOriginEuro().getEDraw() < 3.22f) {
						firstOption = ResultGroup.One;
					}
				}
			}
		} else if (current.getPanKou() >= -0.25) {
			if (isAomenMajor) {
				if (rank.getLRank() >= 10
						&& downChange <= 0.065f
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Middle.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& aaLoseDiff < 0.021f && aomenLoseChange < 0.011f
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.02f
						&& aleLoseDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaLoseDiff < -0.015f || jaLoseDiff <= -0.03f || rank.getLRank() >= 14)
							&& loseNegatives(jaLoseDiff, aaLoseDiff, downChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Zero;
					}
				}
				if (rank.getDRank() >= 8
						&& (aaDrawDiff < 0.011f && aomenDrawChange < 0.019f 
								|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.21f)
						&& jaDrawDiff <= 0.025f && jcDrawChange < 0.005f 
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (aomen.getOriginEuro().getEDraw() <= 3.30f
								&& rank.getDRank() >= 10
								&& (aaDrawDiff <= -0.001 && jaDrawDiff <= -0.01)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() >= PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.025f && jcWinChange < 0.005f
						&& aleWinDiff <= 0.03f
						&& rank.getDRank() <= 11
						) {
					if (aaWinDiff < -0.03f || jaWinDiff < -0.03f) {
						if (firstOption != null){
							secondOption = ResultGroup.Three;
						} else if (rank.getWRank() >= 10
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.03f || jcLoseChange >= 0.01f)) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if (rank.getLRank() >= 10
						&& downChange <= 0.065f
						&& (pmPkDiff >= -0.3f && pcPkDiff >= -0.3f)
						&& (pkDirection.ordinal() < PKDirection.Up.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& aaLoseDiff < 0.011f && aomenLoseChange < 0.011f
						&& waLoseDiff <= 0.035f
						&& jaLoseDiff <= -0.025f
						&& aleLoseDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaLoseDiff < -0.025f || jaLoseDiff <= -0.045f - winAdjByPull
							|| rank.getLRank() >= 14
							|| pkDirection.ordinal() < PKDirection.Middle.ordinal())
							&& loseNegatives(jaLoseDiff, aaLoseDiff, downChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Zero;
					}
				}
				if (rank.getDRank() >= 8
						&& (aaDrawDiff <= -0.001f && aomenDrawChange < 0.019f
							|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.21f)
						&& jaDrawDiff < 0.005f + 2 * winAdjByPull && jcDrawChange < 0.005f
						&& waDrawDiff <= 0.05f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.20f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.20f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (aomen.getOriginEuro().getEDraw() <= 3.30f
								&& rank.getDRank() >= 10
								&& (aaDrawDiff <= -0.005 && jaDrawDiff <= -0.015)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() > PKDirection.Middle.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
//						&& jaWinDiff < 0.005f + 2 * winAdjByPull
						&& jcWinChange < 0.009f
						&& aleWinDiff <= 0.03f
						&& rank.getDRank() <= 11
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.04f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Three;
						} else if (rank.getWRank() >= 10
								&& (aaLoseDiff > -0.019f || jaLoseDiff > -0.03f || jcLoseChange >= 0.01f)
								) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			}
		} else if (current.getPanKou() >= -0.8) {
			if (isAomenMajor) {
				if ((current.getPanKou() >= -0.5 && rank.getLRank() >= 10 || rank.getLRank() >= 12)
						&& downChange <= 0.065f
						&& (pmPkDiff > -0.35f && pcPkDiff > -0.35f)
						&& (pkDirection.ordinal() < PKDirection.Up.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& aaLoseDiff < 0.021f && aomenLoseChange < 0.011f
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.015f
						&& aleLoseDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaLoseDiff < -0.015f || jaLoseDiff <= -0.025f - winAdjByPull
							|| rank.getLRank() >= 14)
							&& loseNegatives(jaLoseDiff, aaLoseDiff, downChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Zero;
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& (aaDrawDiff < 0.011f && aomenDrawChange < 0.019f 
								|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.31f)
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (rank.getDRank() >= 10
								&& aomen.getOriginEuro().getEDraw() <= (current.getPanKou() >= -0.5 ? 3.40f : 3.60f)
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f || jcLoseChange >= 0.010f)
								&& pkDirection.ordinal() > PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& aaWinDiff < 0.035f && jcWinChange < 0.005f
						&& aleWinDiff <= 0.01f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.045f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Three;
						} else if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& rank.getWRank() >= 10
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f)) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if ((current.getPanKou() >= -0.5 && rank.getLRank() >= 10 || rank.getLRank() >= 12)
						&& downChange <= 0.065f
						&& (pmPkDiff > -0.35f && pcPkDiff > -0.35f)
						&& (pkDirection.ordinal() < PKDirection.Up.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& aaLoseDiff < 0.021f && aomenLoseChange < 0.011f
						&& waLoseDiff < 0.035f
						&& jaLoseDiff <= -0.03f
						&& aleLoseDiff <= 0.025f
						&& rank.getDRank() <= 11
						) {
					if ((aaLoseDiff < -0.02f || jaLoseDiff <= -0.04f - winAdjByPull
							|| rank.getLRank() >= 16)
							&& loseNegatives(jaLoseDiff, aaLoseDiff, downChange, pmPkDiff, pcPkDiff) < 2) {
						firstOption = ResultGroup.Zero;
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& (aaDrawDiff <= 0.001f && aomenDrawChange < 0.019f
							|| aaDrawDiff <= 0.021f && aomen.getOriginEuro().getEDraw() <= 3.31f)
						&& jaDrawDiff <= 0.015f + 2 * winAdjByPull && jcDrawChange < 0.005f
						&& waDrawDiff <= 0.05f
						&& aleDrawDiff <= 0.03f
						&& (rank.getDRank() > rank.getLRank() || rank.getDRank() > rank.getWRank())
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f
							|| rank.getDRank() >= 12) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (rank.getDRank() >= 10
								&& aomen.getOriginEuro().getEDraw() <= (current.getPanKou() >= -0.5 ? 3.40f : 3.60f)
								&& (aaLoseDiff > -0.019f || jaLoseDiff > -0.045f || jcLoseChange >= 0.015f)
								&& pkDirection.ordinal() > PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& (pmPkDiff <= 0.3f && pcPkDiff <= 0.3f)
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaWinDiff < -0.021f && aomenWinChange < 0.011f
//						&& jaWinDiff < 0.01f + 2 * winAdjByPull 
						&& jcWinChange < 0.005f
						&& aleWinDiff <= 0.01f
						) {
					if (aaWinDiff <= -0.04f || jaWinDiff < -0.055f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Three;
						} else if (rank.getWRank() >= 10
								&& (aaLoseDiff > -0.019f || jaLoseDiff > -0.045f || jcLoseChange >= 0.015f)
								&& pkDirection.ordinal() > PKDirection.Middle.ordinal()) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			}
		} else {
			if (isAomenMajor) {
				if (rank.getLRank() >= 14
						&& (pmPkDiff >= -0.38f && pcPkDiff >= -0.38f)
						&& (pkDirection.ordinal() < PKDirection.Uper.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff <= 0.03f
						&& jaLoseDiff <= -0.02f
						&& aleLoseDiff <= 0.025f
						) {
					if (aaLoseDiff < -0.02f || jaLoseDiff <= -0.02f - winAdjByPull
							|| rank.getLRank() >= 16) {
						// if pk is -1, then pk value should be low
						if (current.getPanKou() < -1) {
							firstOption = ResultGroup.Zero;
						} else if (downChange <= 0){
							firstOption = ResultGroup.Zero;
						}
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.011f
						&& jaDrawDiff <= 0.035f && jcDrawChange < 0.005f 
						&& aleDrawDiff <= 0.03f
						) {
					if (aaDrawDiff <= -0.025f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.025f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
//								&& rank.getDRank() >= 5
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaWinDiff < -0.001f && aomenWinChange < 0.011f
						&& jaWinDiff < 0.03f && jcWinChange < 0.005f
						&& aleWinDiff <= 0.01f
						) {
					if (aaWinDiff < -0.035f || jaWinDiff < -0.035f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Three;
						} else if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f)) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			} else {
				if (rank.getLRank() >= 14
						&& (pmPkDiff >= -0.38f && pcPkDiff >= -0.38f)
						&& (pkDirection.ordinal() < PKDirection.Uper.ordinal()
								|| pkDirection.ordinal() == PKDirection.Uper.ordinal() && downChange <= 0.04f)
						&& (aaLoseDiff < 0.021f && aomenLoseChange < 0.011f || aaLoseDiff < -0.021f)
						&& waLoseDiff < 0.035f
						&& jaLoseDiff <= -0.03f
						&& aleLoseDiff <= 0.025f
						) {
					if (aaLoseDiff < -0.02f || jaLoseDiff <= -0.04f - winAdjByPull
							|| rank.getLRank() >= 18) {
						// if pk is -1, then pk value should be low
						if (current.getPanKou() < -1) {
							firstOption = ResultGroup.Zero;
						} else if (downChange <= 0){
							firstOption = ResultGroup.Zero;
						}
					}
				}
				if (rank.getDRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaDrawDiff <= -0.001f && aomenDrawChange < 0.011f
						&& aaDrawDiff <= 0.035f && jcDrawChange < 0.005f 
						&& aleDrawDiff <= 0.03f
						) {
					if (aaDrawDiff <= -0.035f || aomen.getOriginEuro().getEDraw() <= 3.30f
							|| jaDrawDiff <= -0.035f || jincai.getCurrentEuro().getEDraw() <= 3.30f) {
						if (firstOption != null) {
							secondOption = ResultGroup.One;
						} else if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f)) {
							firstOption = ResultGroup.One;
						}
					}
				}
				if (rank.getWRank() >= 8
						&& pkDirection.ordinal() > PKDirection.Downer.ordinal()
						&& aaWinDiff < -0.025f && aomenWinChange < 0.011f
//						&& jaWinDiff < 0.03f
						&& jcWinChange < 0.005f
						&& aleWinDiff <= 0.03f
						) {
					if (aaWinDiff < -0.07f || jaWinDiff < -0.07f) {
						if (firstOption != null) {
							secondOption = ResultGroup.Three;
						} else if (pkDirection.ordinal() > PKDirection.Middle.ordinal()
								&& (aaLoseDiff > -0.015f || jaLoseDiff > -0.045f)) {
							firstOption = ResultGroup.Three;
						}
					}
				}
			}
		}

		if (firstOption != null) {
			promote.add(firstOption);
		}
		if (secondOption != null) {
			promote.add(secondOption);
		}
	}
	
	private void analyzeEuroDirection (float aaWinDiff, float aaDrawDiff, float aaLoseDiff,
			float jaWinDiff, float jaDrawDiff, float jaLoseDiff,
			float waWinDiff, float waDrawDiff, float waLoseDiff) {
		
	}
	
	private void findBaseAndPkNotMatched () {
		
	}
	
	private void killByEuro (Set<ResultGroup> killGps, EuroMatrices euMatrices, PankouMatrices pkMatrices, League league) {

		if (euMatrices != null && pkMatrices != null) {
			Map<Company, EuroMatrix> companyEus = euMatrices.getCompanyEus();
			EuroMatrix will = companyEus.get(Company.William);
			EuroMatrix lab = companyEus.get(Company.Ladbrokes);
			EuroMatrix inter = companyEus.get(Company.Interwetten);
			EuroMatrix aomen = companyEus.get(Company.Aomen);
			EuroMatrix ysb = companyEus.get(Company.YiShenBo);
			EuroPl euroAvg = euMatrices.getCurrEuroAvg();
			EuroMatrix jincai = companyEus.get(Company.Jincai);
			EuroMatrix majorComp = EuroUtil.getMainEuro(euMatrices, league);
			
			AsiaPl aomenCurrPk = pkMatrices.getCurrentPk();
			AsiaPl aomenMainPk = pkMatrices.getMainPk();
			float aomenPk = aomenCurrPk.getPanKou();
			float aomenMainCalPK = MatchUtil.getCalculatedPk(aomenMainPk);
			float aomenCurrentCalPK = MatchUtil.getCalculatedPk(aomenCurrPk);
			
			boolean isAomenMajor = EuroUtil.isAomenTheMajor(league);

			if (euroAvg != null && lab != null && majorComp != null && aomen != null) {
				EuroPl currLabEu = lab.getCurrentEuro();
				EuroPl currMajorEu = majorComp.getCurrentEuro();
				EuroPl currAomenEu = aomen.getCurrentEuro();
				EuroPl currWillEu = will.getCurrentEuro();
				EuroPl currInterEu = inter.getCurrentEuro();
				EuroPl currYsbEu = ysb.getCurrentEuro();
				float majorWinChange = majorComp.getWinChange();
				float majorDrawChange = majorComp.getDrawChange();
				float majorLoseChange = majorComp.getLoseChange();
				
				float willWinChange = will.getWinChange();
				float willDrawChange = will.getDrawChange();
				float willLoseChange = will.getLoseChange();
				
				float interWinChange = inter.getWinChange();
				float interDrawChange = inter.getDrawChange();
				float interLoseChange = inter.getLoseChange();
				
				float aomenWinChange = aomen.getWinChange();
				float aomenDrawChange = aomen.getDrawChange();
				float aomenLoseChange = aomen.getLoseChange();
				
				float ysbWinChange = ysb.getWinChange();
				float ysbDrawChange = ysb.getDrawChange();
				float ysbLoseChange = ysb.getLoseChange();

				float laWinRt = MatchUtil.getEuDiff(currLabEu.getEWin(), euroAvg.getEWin(), false);
				float laDrawRt = MatchUtil.getEuDiff(currLabEu.getEDraw(), euroAvg.getEDraw(), false);
				float laLoseRt = MatchUtil.getEuDiff(currLabEu.getELose(), euroAvg.getELose(), false);
				
				float iaWinRt = MatchUtil.getEuDiff(currInterEu.getEWin(), euroAvg.getEWin(), false);
				float iaDrawRt = MatchUtil.getEuDiff(currInterEu.getEDraw(), euroAvg.getEDraw(), false);
				float iaLoseRt = MatchUtil.getEuDiff(currInterEu.getELose(), euroAvg.getELose(), false);
				
				float yaWinRt = MatchUtil.getEuDiff(currYsbEu.getEWin(), euroAvg.getEWin(), false);
				float yaDrawRt = MatchUtil.getEuDiff(currYsbEu.getEDraw(), euroAvg.getEDraw(), false);
				float yaLoseRt = MatchUtil.getEuDiff(currYsbEu.getELose(), euroAvg.getELose(), false);

				float maWinRt = MatchUtil.getEuDiff(currMajorEu.getEWin(), euroAvg.getEWin(), false);
				float maDrawRt = MatchUtil.getEuDiff(currMajorEu.getEDraw(), euroAvg.getEDraw(), false);
				float maLoseRt = MatchUtil.getEuDiff(currMajorEu.getELose(), euroAvg.getELose(), false);
				
				float aaWinRt = MatchUtil.getEuDiff(currAomenEu.getEWin(), euroAvg.getEWin(), false);
				float aaDrawRt = MatchUtil.getEuDiff(currAomenEu.getEDraw(), euroAvg.getEDraw(), false);
				float aaLoseRt = MatchUtil.getEuDiff(currAomenEu.getELose(), euroAvg.getELose(), false);
				
				float waWinRt = MatchUtil.getEuDiff(currWillEu.getEWin(), euroAvg.getEWin(), false);
				float waDrawRt = MatchUtil.getEuDiff(currWillEu.getEDraw(), euroAvg.getEDraw(), false);
				float waLoseRt = MatchUtil.getEuDiff(currWillEu.getELose(), euroAvg.getELose(), false);
				
				float jcWinChange = -1f;
				float jcDrawChange = -1f;
				float jcLoseChange = -1f;
				float jaWinDiff = -1f;
				float jaDrawDiff = -1f;
				float jaLoseDiff = -1f;
				if (jincai != null) {
					jcWinChange = jincai.getWinChange();
					jcDrawChange = jincai.getDrawChange();
					jcLoseChange = jincai.getLoseChange();
					
					jaWinDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEWin(), euroAvg.getEWin(), false);
					jaDrawDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getEDraw(), euroAvg.getEDraw(), false);
					jaLoseDiff = MatchUtil.getEuDiff(jincai.getCurrentEuro().getELose(), euroAvg.getELose(), false);
				}
				
				float pkChange = aomenCurrentCalPK - aomenMainCalPK;
				float jcPkWinDiff = pkChange - (euroAvg.getEWin() - jincai.getCurrentEuro().getEWin());
				float jcPkLoseDiff = pkChange + (euroAvg.getELose() - jincai.getCurrentEuro().getELose());
				
				if (aomenPk >= 1.25f) {
					// ignore
				} else if (aomenPk > 0) {
					if (jcPkWinDiff >= 0.18f) {
						killGps.add(ResultGroup.Three);
					}
				} else if (aomenPk <= -1.25f) {
					// ignore
				} else if (aomenPk < 0) {
					if (jcPkLoseDiff <= -0.18f) {
						killGps.add(ResultGroup.Zero);
					}
				} else {
					if (jcPkWinDiff >= 0.2f) {
						killGps.add(ResultGroup.Three);
					}
					if (jcPkLoseDiff <= -0.2f) {
						killGps.add(ResultGroup.Zero);
					}
				}

				// the main company and lab is higher than the average, or aomen is higher than average and aomen is adjusted high
				if (aomenPk >= 1.25f) {
					// ignore
				} else if (aomenPk >= 1) {
					if (isAomenMajor) {
						if (aaWinRt + aomenWinChange > 0.001
								&& jaWinDiff + jcWinChange > -0.05
								&& willWinChange > 0.001) {
							killGps.add(ResultGroup.Three);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& aaWinRt + aomenWinChange > -0.031
								&& waWinRt > 0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.06 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waWinRt < -0.015
								&& waDrawRt > 0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.041)
								&& waWinRt < -0.015
								&& waLoseRt > 0.021 && willLoseChange > -0.021) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.4) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//									|| yaWinRt > 0.051 && ysbWinChange > -0.001
									|| jaWinDiff > -0.031 && jcWinChange > -0.001)
								&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.021 && aomenDrawChange > -0.001
//									|| yaDrawRt > 0.051 && ysbDrawChange > -0.012
									|| jaDrawDiff > 0.035 && jcDrawChange > -0.012)
								&& yaDrawRt > 0.01 && aaDrawRt > -0.012 && aomenDrawChange > -0.012) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//									|| yaLoseRt > 0.051 && ysbLoseChange > -0.012
									|| jaLoseDiff > 0.051 && jcLoseChange > -0.012)
								&& yaLoseRt > 0.01 && aaLoseRt > -0.01) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.02 && majorLoseChange < 0.02
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.02
								|| MatchContinent.Euro == league.getContinent() ? aaWinRt > 0.005 : aaWinRt > 0.021  // aomen follows will
									&& aomenWinChange > -0.01 && aomenLoseChange < 0.02)
								&& (aaWinRt + aomenWinChange > -0.031)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.05 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.01
								|| aaDrawRt > 0.02 && aomenDrawChange > -0.01)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.02
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.02
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.02)
								&& (aaLoseRt + aomenLoseChange > -0.04)
								&& waLoseRt > -0.011 && willLoseChange > -0.021) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0.25) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > -0.011 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.025 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.051 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015 && currAomenEu.getEDraw() >= 3.15f) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.041 && ysbLoseChange > -0.012
								|| jaLoseDiff > 0.041 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.051 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaWinRt > 0.005 : aaWinRt > 0.021  // aomen follows will
										&& aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.035)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.041)
								&& waLoseRt > -0.011 && willLoseChange > -0.021) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= 0) {
					if (isAomenMajor) {
						if (aaWinRt > 0.019 && aomenWinChange > -0.001
								&& jaWinDiff + jcWinChange > -0.001
								&& willWinChange > 0.001) {
							killGps.add(ResultGroup.Three);
						}
						if (aaDrawRt > 0.021 && aomenDrawChange > -0.001
								&& jaDrawDiff + jcDrawChange > 0.001
								&& waDrawRt > 0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if (aaLoseRt > 0.019 && aomenLoseChange > -0.001
								&& jaLoseDiff + jcLoseChange > -0.001
								&& laLoseRt > 0.031) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if (maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.02
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021
								&& (aaWinRt + aomenWinChange > -0.031)
								&& waWinRt > -0.011 && willWinChange > -0.011) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.011) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.06 && majorLoseChange > -0.021 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -0.25) {
					if (isAomenMajor) {
						if ((aaWinRt > 0.021 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > 0.041 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.025 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.051 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015 && currAomenEu.getEDraw() >= 3.15f) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.041 && ysbLoseChange > -0.012
								|| jaLoseDiff > -0.011 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.041)
								&& waWinRt > -0.011 && willWinChange > -0.021) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.51 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaLoseRt > 0.005 : aaLoseRt > 0.021
										&& aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else if (aomenPk >= -1) {
					if (isAomenMajor) {
						if ((aaWinRt > -0.001 && aomenWinChange > -0.001
//								|| yaWinRt > 0.041 && ysbWinChange > -0.001
								|| jaWinDiff > 0.001 && jcWinChange > -0.001)
							&& yaWinRt > 0.01 && aaWinRt > -0.01) {
							killGps.add(ResultGroup.Three);
						}
						if ((aaDrawRt > 0.021 && aomenDrawChange > -0.001
//								|| yaDrawRt > 0.041 && ysbDrawChange > -0.012
								|| jaDrawDiff > 0.041 && jcDrawChange > -0.012)
							&& yaDrawRt > 0.01 && aaDrawRt > -0.015) {
							killGps.add(ResultGroup.One);
						}
						if ((aaLoseRt > 0.021 && aomenLoseChange > -0.001
//								|| yaLoseRt > 0.045 && ysbLoseChange > -0.012
								|| jaLoseDiff > -0.031 && jcLoseChange > -0.012)
							&& yaLoseRt > 0.01 && aaLoseRt > -0.015) {
							killGps.add(ResultGroup.Zero);
						}
					} else {
						if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
								|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
								|| aaWinRt > 0.021 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
								&& (aaWinRt + aomenWinChange > -0.041)
								&& waWinRt > -0.011 && willWinChange > -0.021) {
							killGps.add(ResultGroup.Three);
						}
						if ((maDrawRt > 0.051 && majorDrawChange > -0.025
								|| iaDrawRt > 0.051 && interDrawChange > -0.011
								|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
								&& (aaDrawRt + aomenDrawChange > -0.031)
								&& (iaDrawRt + interDrawChange > -0.021)
								&& waDrawRt > -0.011 && willDrawChange > -0.02) {
							killGps.add(ResultGroup.One);
						}
						if ((maLoseRt > 0.51 && majorLoseChange > -0.025 && majorWinChange < 0.021
								|| iaLoseRt > 0.051 && interLoseChange > 0.011 && interWinChange < 0.021
								|| MatchContinent.Euro == league.getContinent() ? aaLoseRt > 0.005 : aaLoseRt > 0.021
										&& aomenLoseChange > -0.011 && aomenWinChange < 0.021)
								&& (aaLoseRt + aomenLoseChange > -0.031)
								&& waLoseRt > -0.011 && willLoseChange > -0.011) {
							killGps.add(ResultGroup.Zero);
						}
					}
				} else {
					/*
					if ((maWinRt > 0.06 && majorWinChange > -0.021 && majorLoseChange < 0.021
							|| iaWinRt > 0.051 && interWinChange > 0.011 && interLoseChange < 0.021
							|| aaWinRt > 0.011 && aomenWinChange > -0.011 && aomenLoseChange < 0.021)
							&& (aaWinRt + aomenWinChange > -0.041)
							&& aaLoseRt < -0.015
							&& waWinRt > 0.021 && willWinChange > -0.021) {
						killGps.add(ResultGroup.Three);
					}
					if ((maDrawRt > 0.051 && majorDrawChange > -0.025
							|| iaDrawRt > 0.051 && interDrawChange > -0.011
							|| aaDrawRt > 0.021 && aomenDrawChange > -0.011)
							&& (aaDrawRt + aomenDrawChange > -0.031)
							&& (iaDrawRt + interDrawChange > -0.021)
							&& aaLoseRt < -0.015
							&& waDrawRt > 0.011 && willDrawChange > -0.02) {
						killGps.add(ResultGroup.One);
					}
					if ((maLoseRt > 0.061 && majorLoseChange > -0.025 && majorWinChange < 0.021
							|| iaLoseRt > 0.061 && interLoseChange > 0.011 && interWinChange < 0.021
							|| aaLoseRt > 0.021 && aomenLoseChange > -0.011 && aomenWinChange < 0.021)
							&& (aaLoseRt + aomenLoseChange > -0.031)
							&& waLoseRt > 0.011 && willLoseChange > -0.011) {
						killGps.add(ResultGroup.Zero);
					}
					*/
				}
			}
		}
	}
	
	private void killByPull (float predictPk, float pankou,float currentPk, float mainPk, MatchPull pull, Set<ResultGroup> killByPull) {
		float l1Diff = Math.abs(pankou) >= 0.75 ? 0.13f : 0.07f;
		float l2Diff = Math.abs(pankou) >= 0.75 ? 0.2f : 0.15f;
		
		if (predictPk - mainPk >= l1Diff && predictPk - currentPk >= l1Diff && pull.getHPull() >= 5 && currentPk - mainPk <= 0.08) {
			if ((pull.getHPull() >= 8
						|| predictPk - mainPk >= l1Diff + 0.03f && predictPk - currentPk >= l1Diff + 0.03f
						|| predictPk - currentPk >= l2Diff)
					&& pankou >= -0.5) {
				killByPull.add(ResultGroup.Three);
			}
		} else if (mainPk - predictPk >= l1Diff && currentPk - predictPk >= l1Diff && pull.getGPull() >= 5 && currentPk - mainPk >= -0.08) {
			if ((pull.getGPull() >= 8
						|| mainPk - predictPk >= l1Diff + 0.03f && currentPk - predictPk >= l1Diff + 0.03f
						|| currentPk - predictPk >= l2Diff)
					&& pankou <= 0.5) {
				killByPull.add(ResultGroup.Zero);
			}
		}
	}
	
	private void killByPk (Set<ResultGroup> killByPk, MatchRank rank, MatchPull pull,
			PankouMatrices pkMatrices, Float predictPk) {
		if (pkMatrices != null && predictPk != null) {
			AsiaPl origin = pkMatrices.getOriginPk();
			AsiaPl main = pkMatrices.getMainPk();
			AsiaPl current = pkMatrices.getCurrentPk();
			Float upChange = pkMatrices.getHwinChangeRate();
			Float downChange = pkMatrices.getAwinChangeRate();
			float mainPk = MatchUtil.getCalculatedPk(main);
			float currentPk = MatchUtil.getCalculatedPk(current);
			PKDirection pkDirection = PanKouUtil.getPKDirection(current, main);

			// the predict and main is nearly same, but the company chooses high pay to stop betting on win/lose.
			if ((pkDirection.ordinal() < PKDirection.Middle.ordinal() || current.gethWin() > 1.02)
					&& upChange >= 0.06f
					&& currentPk - mainPk <= 0.04f
					&& Math.abs(predictPk - mainPk) < 0.29) {

				if (current.getPanKou() >= 1.0f && rank.getWRank() <= 9) {
					killByPk.add(ResultGroup.RangThree);
				} else if (current.getPanKou() >= 0.5f && rank.getWRank() <= 8) {
					killByPk.add(ResultGroup.Three);
				} else if (current.getPanKou() >= -0.25f && rank.getWRank() <= 7) {
					killByPk.add(ResultGroup.Three);
				} else if (current.getPanKou() <= -0.5f) {
					if (rank.getWRank() <= 6) {
						killByPk.add(ResultGroup.Three);
					}
					if (rank.getDRank() <= 6) {
						killByPk.add(ResultGroup.One);
					}
				}
			}
			
			if ((pkDirection.ordinal() > PKDirection.Middle.ordinal() || current.getaWin() > 1.02)
					&& downChange >= 0.06f
					&& currentPk - mainPk >= -0.04f
					&& Math.abs(predictPk - mainPk) < 0.29) {

				if (current.getPanKou() <= -1.0f && rank.getLRank() <= 9) {
					killByPk.add(ResultGroup.RangZero);
				} else if (current.getPanKou() <= -0.5f && rank.getLRank() <= 8) {
					killByPk.add(ResultGroup.Zero);
				} else if (current.getPanKou() <= 0.25f && rank.getLRank() <= 7) {
					killByPk.add(ResultGroup.Zero);
				} else if (current.getPanKou() >= 0.5f) {
					if (rank.getLRank() <= 6) {
						killByPk.add(ResultGroup.Zero);
					}
					if (rank.getDRank() <= 6) {
						killByPk.add(ResultGroup.One);
					}
				}
			}
		}
	}
	
	private void killPkPlUnmatchChange (AsiaPl aomenPk, EuroPl currAomenEu, League le, Set<ResultGroup> killGps) {
		float calculatedPk = MatchUtil.getCalculatedPk(aomenPk);
		
		if (aomenPk.getPanKou() == 1.00f) {
			float avg = 1.48f;
			switch (le.getContinent()) {
				case Euro: avg = 1.472f; break;
				case Asia: avg = 1.473f; break;
				case America: avg = 1.490f; break;
				default: avg = 1.48f; break;
			}
			float euPkDiff = (currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk);
			// 1.48-0.12 < pl < 1.48+0.12; 0.8 < pk < 1.2
			if ( euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Three);
			}
		} else if (aomenPk.getPanKou() == 0.75f) {
			float avg = 1.65f;
			switch (le.getContinent()) {
				case Euro: avg = 1.647f; break;
				case Asia: avg = 1.639f; break;
				case America: avg = 1.638f; break;
				default: avg = 1.65f; break;
			}
			float euPkDiff = (currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk);
			// 1.65-0.15 < pl < 1.65+0.15; 0.6 < pk < 0.9
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == 0.50f) {
			float avg = 1.88f;
			switch (le.getContinent()) {
				case Euro: avg = 1.891f; break;
				case Asia: avg = 1.890f; break;
				case America: avg = 1.868f; break;
				default: avg = 1.88f; break;
			}
			float euPkDiff = (currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk);
			// 1.88-0.18 < pl < 1.88+0.18; 0.35 < pk < 0.65
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == 0.25f) {
			float avg = 2.15f;
			switch (le.getContinent()) {
				case Euro: avg = 2.155f; break;
				case Asia: avg = 2.175f; break;
				case America: avg = 2.147f; break;
				default: avg = 2.15f; break;
			}
			float euPkDiff = (currAomenEu.getEWin() - avg) - (aomenPk.getPanKou() - calculatedPk);
			// 2.15-0.2 < pl < 2.15+0.2; 0.1 < pk < 0.4
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Three);
			}			
		} else if (aomenPk.getPanKou() == -0.25f) {
			float avg = 2.18f;
			switch (le.getContinent()) {
				case Euro: avg = 2.176f; break;
				case Asia: avg = 2.201f; break;
				case America: avg = 2.202f; break;
				default: avg = 2.18f; break;
			}
			float euPkDiff = (currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou());
			// 2.18-0.2 < pl < 2.18+0.2; -0.1 > pk > -0.4
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -0.50f) {
			float avg = 1.90f;
			switch (le.getContinent()) {
				case Euro: avg = 1.895f; break;
				case Asia: avg = 1.910f; break;
				case America: avg = 1.909f; break;
				default: avg = 1.90f; break;
			}
			
			float euPkDiff = (currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou());
			// 1.90-0.18 < pl < 1.90+0.18; -0.35 > pk > -0.65
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -0.75f) {
			float avg = 1.65f;
			switch (le.getContinent()) {
				case Euro: avg = 1.646f; break;
				case Asia: avg = 1.665f; break;
				case America: avg = 1.670f; break;
				default: avg = 1.65f; break;
			}
			float euPkDiff = (currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou());
			// 1.65-0.18 < pl < 1.65+0.18; -0.6 > pk > -0.9
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Zero);
			}			
		} else if (aomenPk.getPanKou() == -1.0f) {
			float avg = 1.47f;
			switch (le.getContinent()) {
				case Euro: avg = 1.474f; break;
				case Asia: avg = 1.480f; break;
				case America: avg = 1.450f; break;
				default: avg = 1.47f; break;
			}
			float euPkDiff = (currAomenEu.getELose() - avg) - (calculatedPk - aomenPk.getPanKou());
			// 1.47-0.18 < pl < 1.47+0.18; -0.85 > pk > -1.15
			if (euPkDiff >= 0.08 || euPkDiff <= -0.1) {
				killGps.add(ResultGroup.Zero);
			}			
		}
	}
	
	private float calculateWinDegree (ClubMatrix hostMatrix, ClubMatrix guestMatrix,
			float hostAttGuestDefComp, float guestAttHostDefComp) {
		float rawDiff = FACTOR_H * hostAttGuestDefComp - FACTOR_G * guestAttHostDefComp;
		float winRt = hostMatrix.getWinRt() * 0.7f + (1 - guestMatrix.getWinDrawRt()) * 0.3f;
		float loseRt = guestMatrix.getWinRt() * 0.6f + (1 - hostMatrix.getWinDrawRt()) * 0.4f;
		float diff = rawDiff;
		
		float diffFromWinRt = (winRt * 32 - 10)/16;
		float diffFromLoseRt = (loseRt * 32 - 10)/16;
		
		if (rawDiff > 0) {
			diff = (rawDiff + diffFromWinRt) / 2;
		} else {
			diff = (rawDiff - diffFromLoseRt) / 2;
		}
		
		return diff;
	}
	
	private int adjustDrawDegreeReferToDrawRate (float diff, int totalDegree, ClubMatrix hostMatrix, ClubMatrix guestMatrix, 
			float hostAttGuestDefComp, float guestAttHostDefComp) {
		int drawDegree = (totalDegree + 1) / 2;
		int restDegree = totalDegree - drawDegree;
		
		float hGoalPerMatch = (float)hostMatrix.getGoals() / hostMatrix.getNum();
		float gGoalPerMatch = (float)guestMatrix.getGoals() / guestMatrix.getNum();
		float hMissPerMatch = (float)hostMatrix.getMisses() / hostMatrix.getNum();
		float gMissPerMatch = (float)guestMatrix.getMisses() / guestMatrix.getNum();

		// diff is big, just consider of draw, if small, then check the opposite
		if (diff >= 0) {
			// host and guest are both good, and the goal diff is not high
			if (hostMatrix.getWinDrawRt() >= 0.7 && guestMatrix.getWinDrawRt() >= 0.5) {
				drawDegree ++;
				restDegree --;
			}
			// host is good because of goal too much but match lost rate is high, and guest win rate is high
			else if (hostMatrix.getWinDrawRt() <= 0.7 && guestMatrix.getWinRt() >= 0.4 && diff < 0.5) {
				drawDegree --;
				restDegree ++;
			}
			
			// guest goals more -> guest win more possible
			if (gGoalPerMatch > 1.4f) {
				// host loses much
				if (hostMatrix.getWinDrawRt() <= 0.7 && diff < 0.5) {
					drawDegree --;
					restDegree ++;
					
					if (guestAttHostDefComp >= 1.5) {
						drawDegree --;
						restDegree ++;
					}
				}
			} else if (hGoalPerMatch < 1.55) {
				if (gMissPerMatch < 1.45f && guestMatrix.getWinDrawRt() >= 0.55) {
					drawDegree ++;
					restDegree --;
					
					if (hostAttGuestDefComp <= 1.55) {
						drawDegree ++;
						restDegree --;
					}
				}
			}
		} else {
			// host and guest are both good, and the goal diff is not high
			if (guestMatrix.getWinDrawRt() >= 0.7 && hostMatrix.getWinDrawRt() >= 0.57) {
				drawDegree ++;
				restDegree --;
			}
			// host is good because of goal too much and match lose rate is high, guest win rate is high
			else if (guestMatrix.getWinDrawRt() <= 0.7 && hostMatrix.getWinRt() >= 0.5 && diff > -0.5) {
				drawDegree --;
				restDegree ++;
			}
			
			// host goals more -> host win more possible
			if (hGoalPerMatch > 1.45f) {
				// host loses much
				if (guestMatrix.getWinDrawRt() <= 0.65 && diff > -0.5) {
					drawDegree --;
					restDegree ++;
					
					if (hostAttGuestDefComp > 1.55) {
						drawDegree --;
						restDegree ++;
					}
				}
			} else if (gGoalPerMatch < 1.45) {
				if (hMissPerMatch < 1.45f && hostMatrix.getWinDrawRt() >= 0.57f) {
					drawDegree ++;
					restDegree --;
					
					if (guestAttHostDefComp < 1.45) {
						drawDegree ++;
						restDegree --;
					}
				}
			}
		}
		
		return drawDegree;
	}
	
	private float adjustEuroDiffReferPull (MatchPull pull) {
		float wAdj = 0f;
		if (pull.getHPull() >= 8) {
			wAdj -= 0.01f;
		} else if (pull.getHPull() >= 4) {
			wAdj -= 0.005f;
		} else if (pull.getGPull() >= 8) {
			wAdj += 0.01f;
		} else if (pull.getGPull() >= 4) {
			wAdj += 0.005f;
		}
		return wAdj;
	}
	
	private int winNegatives (float jaDiff, float aaDiff, float pkUpChange, float pmPkDiff, float pcPkDiff) {
		int cnt = 0;
		
		if (jaDiff > -0.025) {
			cnt++;
		}
		if (aaDiff > -0.005) {
			cnt++;
		}
		if (pkUpChange > 0.04) {
			cnt++;
		}
		float paPkDiff = 0.5f * (pmPkDiff + pcPkDiff);
		if (paPkDiff >= 0.08) {
			cnt++;
		}
		return cnt;
	}
	
	private int loseNegatives (float jaDiff, float aaDiff, float pkDownChange, float pmPkDiff, float pcPkDiff) {
		int cnt = 0;
		
		if (jaDiff > -0.025) {
			cnt++;
		}
		if (aaDiff > -0.005) {
			cnt++;
		}
		if (pkDownChange > 0.04) {
			cnt++;
		}
		float paPkDiff = 0.5f * (pmPkDiff + pcPkDiff);
		if (paPkDiff <= -0.08) {
			cnt++;
		}
		return cnt;
	}
	
	@Data
	public static class MatchRank {
		private int wRank;
		private int dRank;
		private int lRank;
		
		public String toString(){
			return wRank + "-" + dRank + "-" + lRank;
		}
	}
	
	@Data
	public static class MatchPull {
		private float hPull;
		private float gPull;
	}

	public static void main (String args[]) {
		predictDraw(3.5833f, 0.2236f);
		predictDraw(2.5833f, 0.9236f);
		predictDraw(1.2534f, 1.9666f);
		predictDraw(1.3714f, 1.3000f);
		predictDraw(1.6214f, 1.4500f);
		predictDraw(1.1114f, 1.2500f);
		predictDraw(1.0577f, 0.4571f);
	}
	
	private static void predictDraw (float a, float b) {
		float dRate = 0;
		double[] temp = new double[]{a, b};
		double devariance = FastMath.sqrt(StatUtils.populationVariance(temp));
		double mean = StatUtils.mean(temp);
		
		dRate += 0.4f;
		dRate -= devariance * mean * 0.618;
		
		System.out.println(a + "   " + b + "    " + devariance  + "    "  + mean  + "    "+ devariance * mean * 0.382 + "   " + dRate);
	}
}
