package com.roy.football.match.OFN;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.DaxiaoMatrices;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

@Component
public class DaxiaoCalculator extends AbstractBaseDataCalculator implements Calculator<DaxiaoMatrices, OFNMatchData> {

	@Override
	public DaxiaoMatrices calucate(OFNMatchData matchData) {
		if (matchData != null) {
			DaxiaoMatrices dxMatrices = new DaxiaoMatrices();
			
			List<AsiaPl> pks = matchData.getDaxiao();
			Date matchDt = matchData.getMatchTime();
			
			if (pks != null && pks.size() > 0) {
				dxMatrices.setOriginPk(pks.get(0));
				AsiaPl latestPl = pks.get(pks.size() - 1);
				dxMatrices.setCurrentPk(latestPl);
				float medianPk = 0.5f * (latestPl.gethWin() + latestPl.getaWin());
				
				AsiaPl main = null;
				float maxHours = 0;
				AsiaPl temp = null;
				float winWeight = 0;
				float loseWeight = 0;
				float changePk = 0;
				float totalHours = 0;

				for (AsiaPl pk : pks) {
					if (temp != null) {
						Date thisDt = pk.getPkDate();
						Date lastDt = temp.getPkDate();

						float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
						float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
						float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);
						float pkWeightByHours = getPkWeightByHours(lastTimeToMatch, thisTimeToMatch);

						// the latest(in 24h) long hours's pankou
						if (lastTimeToMatch > 24) {
							tempHours = 24 - thisTimeToMatch;
							winWeight += (temp.gethWin() - medianPk - changePk) * pkWeightByHours;
							loseWeight += (temp.getaWin() - medianPk + changePk) * pkWeightByHours;
							maxHours = tempHours;
							totalHours += tempHours > 0 ? tempHours : 0;
							main = temp;
						} else if (lastTimeToMatch > 0.5) {
							if (tempHours >= maxHours) {
								maxHours = tempHours;
								main = temp;
							}

							winWeight += (temp.gethWin() - medianPk - changePk) * pkWeightByHours;
							loseWeight += (temp.getaWin() - medianPk + changePk) * pkWeightByHours;
							totalHours += tempHours;
						}
						
						if (pk.getPanKou() > temp.getPanKou()) {
							changePk = 0.125f;
						} else if (pk.getPanKou() < temp.getPanKou()) {
							changePk = -0.125f;
						} else {
							// week the rate
							changePk = changePk * 0.9f;
						}
					}

					temp = pk;
				}
				
				float currentHours = MatchUtil.getDiffHours(matchDt, temp.getPkDate());
				
				if (currentHours >= 0.5) {
					float pkWeightByHours = getPkWeightByHours(currentHours, 0);
					winWeight += (temp.gethWin() - medianPk - changePk) * pkWeightByHours;
					loseWeight += (temp.getaWin() - medianPk + changePk) * pkWeightByHours;
					totalHours += currentHours;
				}

				if (currentHours >= maxHours) {
					main = temp;
				}

				dxMatrices.setMainPk(main);
				dxMatrices.setDaChangeRate(winWeight / (totalHours * 0.85f));
				dxMatrices.setXiaoChangeRate(loseWeight / (totalHours * 0.85f));
				dxMatrices.setHours(totalHours);
				return dxMatrices;
			}
		}

		return null;
	}


	@Override
	public void calucate(DaxiaoMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}

}
