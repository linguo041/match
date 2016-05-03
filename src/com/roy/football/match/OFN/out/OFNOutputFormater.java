package com.roy.football.match.OFN.out;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices.ClubMatrix;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
import com.roy.football.match.OFN.statics.matrices.PredictResult;
import com.roy.football.match.OFN.statics.matrices.MatchState.LatestMatchMatrices;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.MatchUtil;

public class OFNOutputFormater {
	private static NumberFormat nf = NumberFormat.getInstance();

	public OFNExcelData format (OFNMatchData ofnMatch, OFNCalculateResult calculateResult) {
		OFNExcelData excelData = new OFNExcelData();
		excelData.setMatchDayId(ofnMatch.getMatchDayId());
		excelData.setMatchTime(DateUtil.formatDateWithDataBase(ofnMatch.getMatchTime()));
		excelData.setLeagueName(ofnMatch.getLeagueName());
		excelData.setHostName(ofnMatch.getHostName());
		excelData.setGuestName(ofnMatch.getGuestName());

		if (calculateResult != null) {
			ClubMatrices matrices = calculateResult.getClubMatrices();
			if (matrices != null) {
				excelData.setHostLevel(getHostLevel(matrices.getHostLevel(), matrices.getHostAllMatrix()));
				excelData.setGuestLevel(getHostLevel(matrices.getGuestLevel(), matrices.getGuestAllMatrix()));

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
			Float origPk = pkmatrices.getOriginPk().getPanKou();
			
			excelData.setOriginPanKou(String.format("%.2f, %.2f [%.2f]",
					MatchUtil.getCalculatedPk(pkmatrices.getMainPk()), MatchUtil.getCalculatedPk(pkmatrices.getCurrentPk()), origPk));

			excelData.setPkKillRate(String.format("%.2f, %.2f", pkmatrices.getHwinChangeRate(), pkmatrices.getAwinChangeRate()));
			
			EuroMatrices euroMatrics = calculateResult.getEuroMatrices();
			if (euroMatrics != null) {
				excelData.setWillAvgDrawDiff((String.format("%.2f", euroMatrics.getWillAvgDrawDiff())));
			}
			
			PredictResult predictRes = calculateResult.getPredictResult();
			if (predictRes != null) {
				Set<ResultGroup> killByPk = predictRes.getKpResult().getKillByPk();
				Set<ResultGroup> killByPl = predictRes.getKpResult().getKillByPl();
				String kill = "";
				if (killByPk != null && killByPk.size() > 0) {
					kill = getSetVals(killByPk);
				}
				
				if (killByPl != null && killByPl.size() > 0) {
					kill = kill + " | "+ getSetVals(killByPl);
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
