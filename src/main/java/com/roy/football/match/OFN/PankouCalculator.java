package com.roy.football.match.OFN;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

@Component
public class PankouCalculator extends AbstractBaseDataCalculator implements Calculator<PankouMatrices, OFNMatchData> {

	@Override
	public PankouMatrices calucate(OFNMatchData matchData) {
		return calucate(matchData, Company.Aomen);
	}

	@Override
	public void calucate(PankouMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	public PankouMatrices calucate(OFNMatchData matchData, Company company) {
		if (matchData != null) {
			List<AsiaPl> pks = null;
			Date matchDt = matchData.getMatchTime();
			
			switch (company) {
				case Aomen:
					pks = matchData.getAoMen(); break;
				case YiShenBo:
					pks = matchData.getYsb(); break;
				default:
					break;
			}
			
			return calculate(pks, matchDt);
		}
		
		return null;
	}
	
	private PankouMatrices calculate (List<AsiaPl> pks, Date matchDt) {

		if (pks != null && pks.size() > 0) {
			PankouMatrices pkMatrices = new PankouMatrices();
			
			pkMatrices.setOriginPk(pks.get(0));
			AsiaPl latestPl = pks.get(pks.size() - 1);
			
			for (int index = pks.size()-1; index >=0; index--) {
				if (MatchUtil.getDiffHours(matchDt, pks.get(index).getPkDate()) >= 0.3f) {
					latestPl = pks.get(index);
					break;
				} else {
					// remove the pl which is too close the started time, and not quite useful
					if (index > 0) 
						pks.remove(index);
				}
			}
			
			pkMatrices.setCurrentPk(latestPl);
			
			// don't use the median, since company paid pk is not equals 2 but less than 2, usally 1.86
			float medianPk = 0.5f * (latestPl.gethWin() + latestPl.getaWin());
			
			AsiaPl main = pks.get(0);
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
						totalHours += (tempHours > 0 ? tempHours : 0);
						main = temp;
					} else if (lastTimeToMatch > 0.5) {
						if (tempHours >= maxHours) {
							maxHours = tempHours;
							main = temp;
						}

						winWeight += (temp.gethWin() - medianPk - changePk) * pkWeightByHours;
						loseWeight += (temp.getaWin() - medianPk + changePk) * pkWeightByHours;
						totalHours += tempHours;
					} else {
						break;
					}
					
					if (pk.getPanKou() > temp.getPanKou()) {
						changePk = 0.125f;
					} else if (pk.getPanKou() < temp.getPanKou()) {
						changePk = -0.125f;
					} else {
						// week the change if pankou changed
						changePk = changePk * 0.8f;
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

			if (currentHours >= maxHours && temp != null) {
				main = temp;
			}

			pkMatrices.setMainPk(main);
			pkMatrices.setHwinChangeRate(totalHours > 0 ? winWeight / (totalHours * 0.85f) : 0);
			pkMatrices.setAwinChangeRate(totalHours > 0 ? loseWeight / (totalHours * 0.85f) : 0);
			pkMatrices.setHours(totalHours);
			return pkMatrices;
		}
		
		return null;
	}

}
