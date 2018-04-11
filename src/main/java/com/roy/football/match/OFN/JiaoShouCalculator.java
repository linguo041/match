package com.roy.football.match.OFN;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.response.FinishedMatch;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.JiaoShouMatrices;
import com.roy.football.match.base.League;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

@Component
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
			
			int winPkNum = 0;
			int drawPkNum = 0;
			int losePkNum = 0;
			
			int hgoal = 0;
			int ggoal = 0;

			Iterator<FinishedMatch> ite = matches.iterator();

			while (ite.hasNext()) {
				FinishedMatch match = ite.next();
				
				if ((!match.getLeagueId().equals(League.Friendly.getLeagueId()))
						&& MatchUtil.isMatchLatest(match.getMatchTime(), matchData.getMatchTime(), League.getLeagueById(match.getLeagueId()))
						&& match.getMatchTime().getTime() < matchData.getMatchTime().getTime()) {
					
					if (match.getHostId().equals(matchData.getHostId())) {
						if (latestPankou == null) {
							latestPankou = match.getAsiaPanKou();
						}
						
						if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
							winPkNum ++;
						} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
							drawPkNum ++;
						} else {
							losePkNum ++;
						}
						
						if (latestDaxiao == null ) {
							latestDaxiao = MatchUtil.parsePankouString(match.getDaxiaoPanKou());
						}
						
						if (match.getHscore() > match.getAscore()) {
							winNum ++;
						} else if (match.getHscore() == match.getAscore()) {
							drawNum ++;
						} else {
							loseNum ++;
						}
						
						hgoal += match.getHscore();
						ggoal += match.getAscore();
						allNum ++;
					} else {
						if (latestPankou == null) {
							try {
								latestPankou = match.getAsiaPanKou() * -1 + 0.5f;
							} catch (Exception e) {
								// ignore..
							}
						}
						
						if (MatchUtil.UNICODE_WIN.equals(match.getAsiaPanLu())) {
							losePkNum ++;
						} else if (MatchUtil.UNICODE_DRAW.equals(match.getAsiaPanLu())) {
							drawPkNum ++;
						} else {
							winPkNum ++;
						}
						
						if (latestDaxiao == null ) {
							latestDaxiao = MatchUtil.parsePankouString(match.getDaxiaoPanKou());
						}
						
						if (match.getHscore() > match.getAscore()) {
							loseNum ++;
						} else if (match.getHscore() == match.getAscore()) {
							drawNum ++;
						} else {
							winNum ++;
						}
						
						hgoal += match.getAscore();
						ggoal += match.getHscore();
						allNum ++;
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
				matrices.setWinRate((float)winNum/allNum);
				matrices.setWinDrawRate((float)(winNum + drawNum)/allNum);
				matrices.setWinPkRate((float)winPkNum/allNum);
				matrices.setWinDrawPkRate((float)(winPkNum + drawPkNum)/allNum);
				matrices.setHgoalPerMatch((float)hgoal / allNum);
				matrices.setGgoalPerMatch((float)ggoal / allNum);
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
