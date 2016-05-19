package com.roy.football.match.OFN.out;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.MatchUtil;

public class OFNOutputFormater {
	private static NumberFormat nf = NumberFormat.getInstance();

	public OFNExcelData format (OFNMatchData ofnMatch, OFNCalculateResult calculateResult) {
		OFNExcelData excelData = new OFNExcelData();
		excelData.setMatchDayId(ofnMatch.getMatchDayId());
		excelData.setMatchTime(DateUtil.formatDateWithDataBase(ofnMatch.getMatchTime()));
		excelData.setLeagueName(ofnMatch.getLeagueName());
		
		excelData.setMatchInfor(String.format("%s\r\n%s", ofnMatch.getHostName(), ofnMatch.getGuestName()));

		if (calculateResult != null) {
			ClubMatrices matrices = calculateResult.getClubMatrices();
			if (matrices != null) {
				excelData.setLevel(getHostLevel(matrices.getHostLevel(), matrices.getHostAllMatrix())
						+ "\r\n" + getHostLevel(matrices.getGuestLevel(), matrices.getGuestAllMatrix()));

				excelData.setBaseComp(String.format("%.1f : %.1f",
						matrices.getHostAttGuestDefInx(),
						matrices.getGuestAttHostDefInx()));
			}

			MatchState matchState = calculateResult.getMatchState();
			if (matchState != null) {
				excelData.setStateComp(String.format("%.1f : %.1f",
						matchState.getHostAttackToGuest(),
						matchState.getGuestAttackToHost()));
				
				Float hotPoint = calculateResult.getHotPoint();
				excelData.setStateVariation(String.format("%.1f | %.1f, %.1f",
						hotPoint, matchState.getHostAttackVariationToGuest(),
						matchState.getGuestAttackVariationToHost()));
			}

			Float predictPk = calculateResult.getPredictPanKou();
			Float latestPk = null;
			JiaoShouMatrices jiaoshouMatrices = calculateResult.getJiaoShou();

			if (jiaoshouMatrices != null) {
				latestPk = jiaoshouMatrices.getLatestPankou();
				if (jiaoshouMatrices.getMatchNum() > 3) {
					excelData.setJsComp(String.format("%.2f | %.1f : %.1f",
							latestPk > 0.8 ? jiaoshouMatrices.getWinPkRate() : jiaoshouMatrices.getWinRate(),
							jiaoshouMatrices.getHgoalPerMatch(),
							jiaoshouMatrices.getGgoalPerMatch()));
				}
			}
			excelData.setPredictPanKou(getPredictPankouString(predictPk, latestPk));

			PankouMatrices pkmatrices = calculateResult.getPkMatrices();
			
			if (pkmatrices != null) {
				Float mainPk = pkmatrices.getMainPk().getPanKou();
				Float currPk = pkmatrices.getCurrentPk().getPanKou();
				
				excelData.setOriginPanKou(String.format("%.2f [%.2f]\r\n%.2f [%.2f]",
						MatchUtil.getCalculatedPk(pkmatrices.getMainPk()), mainPk,
						MatchUtil.getCalculatedPk(pkmatrices.getCurrentPk()), currPk));

				excelData.setPkKillRate(String.format("%.2f, %.2f", pkmatrices.getHwinChangeRate(), pkmatrices.getAwinChangeRate()));
			}

			EuroMatrices euroMatrics = calculateResult.getEuroMatrices();
			if (euroMatrics != null) {
				League league = League.getLeagueById(ofnMatch.getLeagueId());
				EuroMatrix majorComp = MatchUtil.getMainEuro(euroMatrics, league);

				if (majorComp != null) {
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
				EuroMatrix jincai = euroMatrics.getJincaiMatrix();
				if (euAvg != null) {
					euAvgWin = euAvg.geteWin();
					euAvgDraw = euAvg.geteDraw();
					euAvgLose = euAvg.geteLose();
				}
				if (jincai != null && jincai.getCurrentEuro() != null) {
					euJcWin = jincai.getCurrentEuro().geteWin();
					euJcDraw = jincai.getCurrentEuro().geteDraw();
					euJcLose = jincai.getCurrentEuro().geteLose();
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
			}
			
			MatchExchangeData exgData = calculateResult.getExchanges();
			if (exgData != null) {
				if (exgData.getBfWinExchange() != null && exgData.getBfWinExchange() + exgData.getBfDrawExchange() + exgData.getBfLoseExchange() > 200000) {
					excelData.setBifa(String.format("%.1f : %.1f : %.1f\n%.1f : %.1f : %.1f",
							exgData.getBfWinExgRt() * 100,
							exgData.getBfDrawExgRt() * 100,
							exgData.getBfLoseExgRt() * 100,
							exgData.getJcWinExgRt() * 100,
							exgData.getJcDrawExgRt() * 100,
							exgData.getJcLoseExgRt() * 100));
				}

				if (exgData.getJcWinExchange() != null && exgData.getJcTotalExchange() > 900000) {
					excelData.setBifa(String.format("%.1f : %.1f : %.1f",
							exgData.getJcWinExgRt() * 100,
							exgData.getJcDrawExgRt() * 100,
							exgData.getJcLoseExgRt() * 100));
					
					excelData.setJincaiJY(String.format("%.2f万\n%d   %d   %d",
							exgData.getJcTotalExchange() / 10000f,
							exgData.getJcWinGain(),
							exgData.getJcDrawGain(),
							exgData.getJcLoseGain()));
				}
			}
			
			PredictResult predictRes = calculateResult.getPredictResult();
			if (predictRes != null) {
				Set<ResultGroup> killByPk = predictRes.getKpResult().getKillByPk();
				Set<ResultGroup> killByPl = predictRes.getKpResult().getKillByPl();
				Set<ResultGroup> killByEx = predictRes.getKpResult().getKillByExchange();
				String kill = "";
				if (killByPk != null && killByPk.size() > 0) {
					kill = getSetVals(killByPk);
				}
				
				if (killByPl != null && killByPl.size() > 0) {
					kill = kill + " |"+ getSetVals(killByPl);
				}
				
				if (killByEx != null && killByEx.size() > 0) {
					kill = kill + " ~"+ getSetVals(killByEx);
				}
				excelData.setKill(kill);
				Set<ResultGroup> promote = predictRes.getKpResult().getPromoteByPk();
				excelData.setPromote(getSetVals(promote));
				
				excelData.setPredictScore(String.format("%.1f : %.1f",
						predictRes.getHostScore(), predictRes.getGuestScore()));
			}

//			excelData
//			TODO
		}
		return excelData;
	}
	
	private String getHostLevel (TeamLevel tl, ClubMatrix baseMatrix) {
		if (tl == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder(tl.name());
		
		if (baseMatrix != null) {
			sb.append('[').append(nf.format(baseMatrix.getWinRt())).append(", ")
				.append(baseMatrix.getGoals() - baseMatrix.getMisses())
				.append(']');
		}

		return sb.toString();
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
}
