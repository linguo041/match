package com.roy.football.match.OFN;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class JiaoShouCalculator extends AbstractBaseDataCalculator implements Calculator<JiaoShouMatrices, OFNMatchData> {

	@Override
	public JiaoShouMatrices calucate(OFNMatchData matchData) {
		List<FinishedMatch> matches = matchData.getJiaoShou();
		
		if (matches != null && matches.size() > 0) {
			Collections.sort(matches);
			
			JiaoShouMatrices matrices = new JiaoShouMatrices();
			
			Float latestPankou = null;
			Float latestDaxiao = null;
			Float tempPankou = null;
			int winNum = 0;         // if host is strong, check this matrix
			int drawNum = 0;     // otherwise check this one
			int loseNum = 0;
			int allNum = 0;

			Iterator<FinishedMatch> ite = matches.iterator();

			while (ite.hasNext()) {
				FinishedMatch match = ite.next();
				
				if (match.getLeagueId().equals(matchData.getLeagueId())) {
					
					if (match.getHostId().equals(matchData.getHostId())) {
						if (latestPankou == null && MatchUtil.isMatchInAYear(match.getMatchTime(), matchData.getMatchTime())) {
							try {
								latestPankou = Float.parseFloat(match.getAsiaPanKou());
							} catch (Exception e) {
								// ignore..
							}
						}
						
						if (latestDaxiao == null ) {
							latestDaxiao = MatchUtil.parsePankouString(match.getDaxiaoPanKou());
						}
						
						if (match.getHscore() > match.getAscore()) {
							winNum ++;
						} else if (match.getHscore() > match.getAscore()) {
							drawNum ++;
						} else {
							loseNum ++;
						}
						
						allNum ++;
					} else {
						if (tempPankou == null && MatchUtil.isMatchInAYear(match.getMatchTime(), matchData.getMatchTime())) {
							try {
								tempPankou = Float.parseFloat(match.getAsiaPanKou()) * -1 + 0.5f;
							} catch (Exception e) {
								// ignore..
							}
						}
					}
				}
			}
			
			if (latestPankou == null) {
				latestPankou = tempPankou;
			}
			
			matrices.setLatestDaxiao(latestDaxiao);
			matrices.setLatestPankou(latestPankou);
			matrices.setMatchNum(allNum);
			
			if (allNum != 0) {
				matrices.setWinRate((float)(winNum/allNum));
				matrices.setWinDrawRate((float)((winNum + drawNum)/allNum));
			}

			return matrices;
		}
		
		return null;
	}

	@Override
	public void calucate(JiaoShouMatrices jiaoShouMatrices,
			OFNMatchData matchData) {
		
	}

}
