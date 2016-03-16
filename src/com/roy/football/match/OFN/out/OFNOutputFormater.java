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
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.OFN.statics.matrices.MatchState;
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
			excelData.setHostLevel(getHostLevel(calculateResult.getHostLevel(), matrices.getHostAllMatrix()));
			excelData.setHostLabel((calculateResult.getHostLabels() == null || calculateResult.getHostLabels().size() <= 0) ? "" : calculateResult.getHostLabels().toString());
			excelData.setGuestLevel(getHostLevel(calculateResult.getGuestLevel(), matrices.getGuestAllMatrix()));
			excelData.setGuestLabel((calculateResult.getGuestLabels() == null || calculateResult.getGuestLabels().size() <= 0) ? "" : calculateResult.getGuestLabels().toString());
		
			JiaoShouMatrices jiaoshouMatrices = calculateResult.getJiaoShou();
			Float predictPk = calculateResult.getPredictPanKou();
			Float latestPk = jiaoshouMatrices == null ? null : jiaoshouMatrices.getLatestPankou();
			excelData.setPredictPanKou(getPredictPankouString(predictPk, latestPk));

			PankouMatrices pkmatrices = calculateResult.getPkMatrices();
			Float origPk = pkmatrices.getOriginPk().getPanKou();
			
			excelData.setOriginPanKou(String.format("%.2f [%.2f, %.2f]",
					origPk, MatchUtil.getCalculatedPk(pkmatrices.getMainPk()), MatchUtil.getCalculatedPk(pkmatrices.getCurrentPk())));
			
			Set<ResultGroup> hot = calculateResult.getTooHot();
			excelData.setTooHot(getSetVals(hot));
			Set<ResultGroup> killByPk = calculateResult.getKillByPk();
			Set<ResultGroup> killByPl = calculateResult.getKillByPl();
			String kill = "";
			if (killByPk != null && killByPk.size() > 0) {
				kill = getSetVals(killByPk);
			}
			
			if (killByPl != null && killByPl.size() > 0) {
				kill = kill + " | "+ getSetVals(killByPl);
			}
			excelData.setKill(kill);
			Set<ResultGroup> promote = calculateResult.getPromote();
			excelData.setPromote(getSetVals(promote));
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
