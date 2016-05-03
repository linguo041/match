package com.roy.football.match.OFN;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class EuroCalculator extends AbstractBaseDataCalculator implements Calculator<EuroMatrices, OFNMatchData>{

	@Override
	public EuroMatrices calucate(OFNMatchData matchData) {
		if (matchData != null) {
			Map<Company, List<EuroPl>> comEuros = matchData.getEuroPls();
			Date matchDt = matchData.getMatchTime();
			
			if (comEuros != null && comEuros.size() > 0) {
				EuroMatrices euroMatrices = new EuroMatrices();
				
				euroMatrices.setWilliamMatrix(getEuroMatrix(comEuros.get(Company.William), matchDt));
				euroMatrices.setAomenMatrix(getEuroMatrix(comEuros.get(Company.Aomen), matchDt));
				euroMatrices.setLadMatrix(getEuroMatrix(comEuros.get(Company.Ladbrokes), matchDt));
				euroMatrices.setYiShenBoMatrix(getEuroMatrix(comEuros.get(Company.YiShenBo), matchDt));
				euroMatrices.setInterwettenMatrix(getEuroMatrix(comEuros.get(Company.Interwetten), matchDt));
				euroMatrices.setSnaiMatrix(getEuroMatrix(comEuros.get(Company.SNAI), matchDt));
				euroMatrices.setCurrEuroAvg(matchData.getEuroAvg());
				euroMatrices.setWillAvgDrawDiff(getWillAvgDrawDiff(euroMatrices.getWilliamMatrix().getCurrentEuro(), euroMatrices.getCurrEuroAvg()));
				
				return euroMatrices;
			}
		}
		return null;
	}

	@Override
	public void calucate(EuroMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}
	
	private EuroMatrix getEuroMatrix (List<EuroPl> euroPls, Date matchDt) {
		if (euroPls != null && euroPls.size() > 0) {
			EuroMatrix euMatrix = new EuroMatrix();
			euMatrix.setOriginEuro(euroPls.get(0));
			euMatrix.setMainEuro(getMainEuro(euroPls, matchDt));
			euMatrix.setCurrentEuro(euroPls.get(euroPls.size()-1));
			return euMatrix;
		}
		return null;
	}
	
	private float getWillAvgDrawDiff (EuroPl currWillEu, EuroPl euroAvg) {
		return MatchUtil.getEuDiff(currWillEu.geteDraw(), euroAvg.geteDraw(), false);
	}
	
	private EuroPl getMainEuro (List<EuroPl> euroPls, Date matchDt) {
		if (euroPls != null && euroPls.size() > 0) {
			EuroPl main = null;
			float hours = 0;
			EuroPl temp = null;
			
			for (EuroPl eu : euroPls) {
				if (temp != null) {
					Date thisDt = eu.geteDate();
					Date lastDt = temp.geteDate();
					
					float tempHours = MatchUtil.getDiffHours(thisDt, lastDt);
					float lastTimeToMatch = MatchUtil.getDiffHours(matchDt, lastDt);
					float thisTimeToMatch = MatchUtil.getDiffHours(matchDt, thisDt);
					
					// the latest(in 24h) long hours's pankou
					if (lastTimeToMatch > 24) {
						hours = 24 - thisTimeToMatch;
						main = temp;
					} else {
						if (tempHours >= hours) {
							hours = tempHours;
							main = temp;
						}
					}
				}
				
				temp = eu;
			}
			
			if (MatchUtil.getDiffHours(new Date(), temp.geteDate()) >= hours) {
				main = temp;
			}

			return main;
		}

		return null;
	}

}
