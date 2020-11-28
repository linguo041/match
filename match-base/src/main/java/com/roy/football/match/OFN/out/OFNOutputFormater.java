package com.roy.football.match.OFN.out;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.MatchPromoter.MatchRank;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.MatchResultAnalyzed;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.OFN.statics.matrices.PromoteMatrics;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.okooo.MatchExchangeData.ExchangeType;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.EuroUtil;
import com.roy.football.match.util.MatchUtil;

@Component
public class OFNOutputFormater {
	private static NumberFormat nf = new DecimalFormat("#.##");

	public OFNExcelData format (OFNMatchData ofnMatch, OFNCalculateResult calculateResult) {
		OFNExcelData excelData = new OFNExcelData();
		excelData.setMatchDayId(ofnMatch.getMatchDayId());
		excelData.setMatchTime(DateUtil.formatDateWithDataBase(ofnMatch.getMatchTime()));
		excelData.setLeagueName(ofnMatch.getLeague().name());
		
		excelData.setMatchInfor(String.format("%s\r\n%s", ofnMatch.getHostName(), ofnMatch.getGuestName()));
		
		if (ofnMatch.getHostScore() != null) {
			excelData.setResult(String.format("%s : %s", ofnMatch.getHostScore(), ofnMatch.getGuestScore()));
		}

		if (calculateResult != null) {
			ClubMatrices matrices = calculateResult.getClubMatrices();
			StringBuilder hostGuestCompFormat = new StringBuilder();
			List <Object> hostGuestCompArgs = new ArrayList<Object>();
			
			StringBuilder samePkHotFormat = new StringBuilder();
			List <Object> samePkHotArgs = new ArrayList<Object>();

			if (matrices != null) {
				excelData.setLevel(getHostLevel(matrices.getHostLevel(), matrices.getHostAllMatrix())
						+ "\r\n" + getHostLevel(matrices.getGuestLevel(), matrices.getGuestAllMatrix()));
				
				hostGuestCompFormat.append("B [%.1f : %.1f | %.1f : %.1f]");
				hostGuestCompArgs.add(matrices.getHostAttGuestDefInx());
				hostGuestCompArgs.add(matrices.getGuestAttHostDefInx());
				hostGuestCompArgs.add(matrices.getHostWinRt());
				hostGuestCompArgs.add(matrices.getGuestWinRt());
			}

			MatchState matchState = calculateResult.getMatchState();
			if (matchState != null) {
				hostGuestCompFormat.append(hostGuestCompFormat.length() > 0 ? '\n' : "");
				hostGuestCompFormat.append("S [%.1f : %.1f | %.1f : %.1f]");
				hostGuestCompArgs.add(matchState.getHostAttackToGuest());
				hostGuestCompArgs.add(matchState.getGuestAttackToHost());
				hostGuestCompArgs.add(matchState.getHostWinRt());
				hostGuestCompArgs.add(matchState.getGuestWinRt());

				Float hotPoint = matchState.getHotPoint();
//				excelData.setStateVariation(String.format("%.1f | %.1f, %.1f",
//						hotPoint, matchState.getHostAttackVariationToGuest(),
//						matchState.getGuestAttackVariationToHost()));
				samePkHotFormat.append("%.1f | %.1f, %.1f");
				samePkHotArgs.add(hotPoint);
				samePkHotArgs.add(matchState.getHostAttackVariationToGuest());
				samePkHotArgs.add(matchState.getGuestAttackVariationToHost());
				
				LatestMatchMatrices hostSame = matchState.getHostSamePk();
				LatestMatchMatrices guestSame = matchState.getGuestSamePk();
				if (hostSame != null) {
					samePkHotFormat.append(samePkHotFormat.length() > 0 ? '\n' : "");
					samePkHotFormat.append("H   %.1f : %.1f | %.2f");
					samePkHotArgs.add(hostSame.getMatchGoal());
					samePkHotArgs.add(hostSame.getMatchMiss());
					samePkHotArgs.add(hostSame.getWinRate());
				}
				if (guestSame != null) {
					samePkHotFormat.append(samePkHotFormat.length() > 0 ? '\n' : "");
					samePkHotFormat.append("G   %.1f : %.1f | %.2f");
					samePkHotArgs.add(guestSame.getMatchGoal());
					samePkHotArgs.add(guestSame.getMatchMiss());
					samePkHotArgs.add(guestSame.getWinDrawRate());
				}
				
				excelData.setStateVariation(String.format(samePkHotFormat.toString(), samePkHotArgs.toArray()));
			}

			Float predictPk = calculateResult.getPredictPanKou();
			Float latestPk = null;
			JiaoShouMatrices jiaoshouMatrices = calculateResult.getJiaoShou();
			float pkBalance = 0f;
			float currentPk = 0f;

			if (jiaoshouMatrices != null) {
				latestPk = jiaoshouMatrices.getLatestPankou();
				if (jiaoshouMatrices.getMatchNum() > 3) {
					hostGuestCompFormat.append(hostGuestCompFormat.length() > 0 ? '\n' : "");
					hostGuestCompFormat.append("J   %.1f : %.1f | %.2f");
					hostGuestCompArgs.add(jiaoshouMatrices.getHgoalPerMatch());
					hostGuestCompArgs.add(jiaoshouMatrices.getGgoalPerMatch());
					hostGuestCompArgs.add(jiaoshouMatrices.getWinRate());
				}
			}

			if (hostGuestCompArgs.size() > 0) {
				excelData.setHostGuestComp(String.format(hostGuestCompFormat.toString(), hostGuestCompArgs.toArray()));
			}
			
//			excelData.setPredictPanKou(getPredictPankouString(predictPk, latestPk));

			PankouMatrices pkmatrices = calculateResult.getPkMatrices();
			
			if (pkmatrices != null) {
				Float mainPk = pkmatrices.getMainPk().getPanKou();
				Float currPk = pkmatrices.getCurrentPk().getPanKou();
				float calculatedCurrPk = MatchUtil.getCalculatedPk(pkmatrices.getCurrentPk());
				pkBalance = (currPk - calculatedCurrPk);
				currentPk = currPk;
				
				excelData.setOriginPanKou(String.format("%.2f [%.2f]\r\n%.2f [%.2f]\r\n%.2f [%.2f]",
						predictPk, latestPk,
						MatchUtil.getCalculatedPk(pkmatrices.getMainPk()), mainPk,
						calculatedCurrPk, currPk));

				excelData.setPkKillRate(String.format("%.2f, %.2f", pkmatrices.getHwinChangeRate(), pkmatrices.getAwinChangeRate()));
			}

			EuroMatrices euroMatrics = calculateResult.getEuroMatrices();
			String euVarianceStr = "";
			if (euroMatrics != null) {
				League league = ofnMatch.getLeague();
				EuroMatrix majorComp = EuroUtil.getMainEuro(euroMatrics, league);
				
				euVarianceStr = String.format("%.2f : %.2f : %.2f\n",
						euroMatrics.getEuWinVariance(),
						euroMatrics.getEuDrawVariance(),
						euroMatrics.getEuLoseVariance());

				if (majorComp != null && (league.getMajorCompany() != Company.Aomen || league.getMajorCompany() != Company.William)) {
					excelData.setPlMatrix((String.format("%s\n%.2f   %.2f   %.2f\n"
							+ "%.2f   %.2f   %.2f",
							league.getMajorCompany(),
							euroMatrics.getMainAvgWinDiff(),
							euroMatrics.getMainAvgDrawDiff(),
							euroMatrics.getMainAvgLoseDiff(),
							majorComp.getWinChange(),
							majorComp.getDrawChange(),
							majorComp.getLoseChange())));
				}

				float euAvgWin = 0;
				float euAvgDraw = 0;
				float euAvgLose = 0;
				float euJcWin = 0;
				float euJcDraw = 0;
				float euJcLose = 0;
				float euJcWinChg = 0;
				float euJcDrawChg = 0;
				float euJcLoseChg = 0;
			

				EuroPl euAvg = euroMatrics.getCurrEuroAvg();
				Map<Company, EuroMatrix> companyEus = euroMatrics.getCompanyEus();
				EuroMatrix jincai = companyEus.get(Company.Jincai);
				EuroMatrix aomen = companyEus.get(Company.Aomen);
				EuroMatrix william = companyEus.get(Company.William);
				
				if (euAvg != null) {
					euAvgWin = euAvg.getEWin();
					euAvgDraw = euAvg.getEDraw();
					euAvgLose = euAvg.getELose();
				}
				if (jincai != null && jincai.getCurrentEuro() != null) {
					euJcWin = jincai.getCurrentEuro().getEWin();
					euJcDraw = jincai.getCurrentEuro().getEDraw();
					euJcLose = jincai.getCurrentEuro().getELose();
					euJcWinChg = jincai.getWinChange();
					euJcDrawChg = jincai.getDrawChange();
					euJcLoseChg = jincai.getLoseChange();
				}

				excelData.setJincai(String.format("%.2f   %.2f   %.2f\n"
												+ "%.2f   %.2f   %.2f\n"
												+ "%.2f   %.2f   %.2f",
						euAvgWin, euAvgDraw, euAvgLose,
						euJcWin, euJcDraw, euJcLose,
						euJcWinChg, euJcDrawChg, euJcLoseChg));
				
				if (william != null && william.getCurrentEuro() != null) {
					float waWinDiff = MatchUtil.getEuDiff(william.getCurrentEuro().getEWin(), euAvgWin, false);
					float waDrawDiff = MatchUtil.getEuDiff(william.getCurrentEuro().getEDraw(), euAvgDraw, false);
					float waLoseDiff = MatchUtil.getEuDiff(william.getCurrentEuro().getELose(), euAvgLose, false);
					float euWillWinChg = william.getWinChange();
					float euWillDrawChg = william.getDrawChange();
					float euWillLoseChg = william.getLoseChange();
					
					excelData.setWill(String.format("%s\n%.2f   %.2f   %.2f\n"
							+ "%.2f   %.2f   %.2f",
						Company.William,
						waWinDiff, waDrawDiff, waLoseDiff,
						euWillWinChg, euWillDrawChg, euWillLoseChg));
				}
				
				float aaWinDiff = 0;
				float aaDrawDiff = 0;
				float aaLoseDiff = 0;
				float aleWinDiff = 0;
				float aleDrawDiff = 0;
				float aleLoseDiff = 0;
				float euAomenWinChg = 0;
				float euAomenDrawChg = 0;
				float euAomenLoseChg = 0;
				String aomenEuroData = "";
				
				if (aomen != null && aomen.getCurrentEuro() != null) {
					aaWinDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEWin(), euAvgWin, false);
					aaDrawDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEDraw(), euAvgDraw, false);
					aaLoseDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getELose(), euAvgLose, false);
					
					euAomenWinChg = aomen.getWinChange();
					euAomenDrawChg = aomen.getDrawChange();
					euAomenLoseChg = aomen.getLoseChange();
					
					EuroPl aLePl = aomen.getLeAvgEuro();
					if (aLePl != null) {
						float winPkBalance = currentPk >= 0 ? pkBalance * 0.85f : pkBalance * 1.5f;
						float losePkBalance = currentPk <= 0 ? pkBalance * 0.85f : pkBalance * 1.5f;
						aleWinDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEWin(), aLePl.getEWin() + winPkBalance, false);
						aleDrawDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getEDraw(), aLePl.getEDraw(), false);
						aleLoseDiff = MatchUtil.getEuDiff(aomen.getCurrentEuro().getELose(), aLePl.getELose() - losePkBalance, false);
						aomenEuroData = String.format("%.2f   %.2f   %.2f\n"
								+ "%.2f   %.2f   %.2f\n"
								+ "%.2f   %.2f   %.2f",
								aleWinDiff, aleDrawDiff, aleLoseDiff,
								aaWinDiff, aaDrawDiff, aaLoseDiff,
								euAomenWinChg, euAomenDrawChg, euAomenLoseChg);
						
					} else {
						aomenEuroData = String.format("%.2f   %.2f   %.2f\n"
								+ "%.2f   %.2f   %.2f",
								aaWinDiff, aaDrawDiff, aaLoseDiff,
								euAomenWinChg, euAomenDrawChg, euAomenLoseChg);
					}
				}

				excelData.setAomen(aomenEuroData);
			}
			
			MatchExchangeData exgData = calculateResult.getExchanges();
			StringBuilder jcExchange = new StringBuilder(euVarianceStr);
			if (exgData != null) {
				String bfjcRate = "";
				
				if (exgData.hasExchangeData(ExchangeType.bf, false) && exgData.getBfWinExchange() + exgData.getBfDrawExchange() + exgData.getBfLoseExchange() > 200000) {
					bfjcRate = String.format("%.1f : %.1f : %.1f\n%.1f : %.1f : %.1f",
							exgData.getBfWinExgRt() * 100,
							exgData.getBfDrawExgRt() * 100,
							exgData.getBfLoseExgRt() * 100,
							exgData.getJcWinExgRt() * 100,
							exgData.getJcDrawExgRt() * 100,
							exgData.getJcLoseExgRt() * 100);
				}

				if (exgData.hasExchangeData(ExchangeType.jc, true) && exgData.getJcTotalExchange() > 900000) {
					bfjcRate = bfjcRate.equals("") ? String.format("%.1f : %.1f : %.1f",
								exgData.getJcWinExgRt() * 100,
								exgData.getJcDrawExgRt() * 100,
								exgData.getJcLoseExgRt() * 100)
							: bfjcRate;
					jcExchange.append(String.format("%.2f万\n%d   %d   %d",
							exgData.getJcTotalExchange() / 10000f,
							exgData.getJcWinGain(),
							exgData.getJcDrawGain(),
							exgData.getJcLoseGain()));
				}

				excelData.setBifa(bfjcRate);
			}
			excelData.setJincaiJY(jcExchange.toString());
			
			PredictResult predictRes = calculateResult.getPredictResult();
			if (predictRes != null) {
				Set<ResultGroup> killByPk = predictRes.getKpResult().getKillByPk();
				Set<ResultGroup> killByPl = predictRes.getKpResult().getKillByPl();
				Set<ResultGroup> killByPlPkUnmatch = predictRes.getKpResult().getKillByPlPkUnmatch();
				Set<ResultGroup> killByPull = predictRes.getKpResult().getKillByPull();
				Set<ResultGroup> killByExchange = predictRes.getKpResult().getKillByExchange();
				String kill = "";
				if (killByPk != null && killByPk.size() > 0) {
					kill = " ~" + getSetVals(killByPk);
				}
				
				if (killByPl != null && killByPl.size() > 0) {
					kill = kill + " !"+ getSetVals(killByPl);
				}
				
				if (killByPlPkUnmatch != null && killByPlPkUnmatch.size() > 0) {
					kill = kill + " @"+ getSetVals(killByPlPkUnmatch);
				}
				
				if (killByPull != null && killByPull.size() > 0) {
					kill = kill + " *"+ getSetVals(killByPull);
				}
				if (killByExchange != null && killByExchange.size() > 0) {
					kill = kill + " e"+ getSetVals(killByExchange);
				}
				excelData.setKill(kill);
				
				Set<ResultGroup> promote = predictRes.getKpResult().getPromoteByBase();
				String promoteStr = getSetVals(promote);
				Set<ResultGroup> particularPromote = predictRes.getKpResult().getPromoteByFixModel();
				if (particularPromote != null && particularPromote.size() > 0) {
					promoteStr = promoteStr + " p" + getSetVals(particularPromote);
				}
				excelData.setPromote(promoteStr);
				
//				excelData.setPredictScore(String.format("%.1f : %.1f",
//						predictRes.getHostScore(), predictRes.getGuestScore()));
				MatchRank rank = predictRes.getKpResult().getRank();
				excelData.setPredictScore(String.format("%d  -  %d  -  %d\n%.2f : %.2f",
						rank.getWRank(), rank.getDRank(), rank.getLRank(),
						predictRes.getHostScore(), predictRes.getGuestScore()));
//				PromoteMatrics promoteMatrics = predictRes.getKpResult().getPromoteMatrics();
//				excelData.setPromoteRatio(promoteMatrics.toString());
				excelData.setPromoteRatio(getMatchResultAnalyzed(calculateResult));
			}

//			excelData
//			TODO
		}
		return excelData;
	}
	
	private String getMatchResultAnalyzed (OFNCalculateResult calculateResult) {
		MatchResultAnalyzed hostMra = calculateResult.getHostMra();
		MatchResultAnalyzed guestMra = calculateResult.getGuestMra();
		
		if (hostMra != null && guestMra != null) {
			return String.format("Time-Shot-SoT-Score|fault-save-Soted\n"
					+ "%.2f - %.1f - %.1f - %.1f | %.1f - %.1f - %.1f\n"
					+ "%.2f - %.1f - %.1f - %.1f | %.1f - %.1f - %.1f",
				hostMra.getHostTime(), hostMra.getHostShot(), hostMra.getHostShotOnTarget(), hostMra.getHostScore(),
				hostMra.getHostFault(), hostMra.getHostSave(), hostMra.getGuestShotOnTarget(),
				guestMra.getHostTime(), guestMra.getHostShot(), guestMra.getHostShotOnTarget(), guestMra.getHostScore(),
				guestMra.getHostFault(), guestMra.getHostSave(), guestMra.getGuestShotOnTarget()
				);
		}
		
		return "";
	}
	
	private String getHostLevel (TeamLevel tl, ClubMatrix baseMatrix) {
		if (tl == null) {
			return "";
		}
		
		return String.format("%s[%s, %s, %d]",
				tl.name(),
				nf.format(baseMatrix.getWinRt()),
				nf.format(baseMatrix.getWinDrawRt() - baseMatrix.getWinRt()),
				baseMatrix.getGoals() - baseMatrix.getMisses());
	}
	
	private String getSetVals (Set<ResultGroup> sets) {
		String s = "";
		
		if (sets != null && sets.size() > 0) {
			Iterator<ResultGroup> its = sets.iterator();
			while (its.hasNext()) {
				s += its.next();
			}
		}

		return s;
	}
	
	private String getPredictPankouString(Float predictPk, Float latestPk) {
		String format = "";
		List<Float> args = new ArrayList<Float>();
		if (predictPk != null) {
			format = "%.2f";
			args.add(predictPk);
		}
		if (latestPk != null) {
			format += " [%.2f]";
			args.add(latestPk);
		}
		return String.format(format, args.toArray());
	}
	
	public static void main (String args[]) {
		System.out.println(nf.format(25.6789));
	}
}
