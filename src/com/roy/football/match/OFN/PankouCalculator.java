package com.roy.football.match.OFN;

import java.util.Date;
import java.util.List;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.PankouMatrices;
import com.roy.football.match.process.Calculator;
import com.roy.football.match.util.MatchUtil;

public class PankouCalculator extends AbstractBaseDataCalculator implements Calculator<PankouMatrices, OFNMatchData> {

	@Override
	public PankouMatrices calucate(OFNMatchData matchData) {
		if (matchData != null) {
			PankouMatrices pkMatrices = new PankouMatrices();
			
			List<AsiaPl> pks = matchData.getAoMen();
			Date matchDt = matchData.getMatchTime();
			
			if (pks != null && pks.size() > 0) {
				pkMatrices.setOriginPk(pks.get(0));
				pkMatrices.setCurrentPk(pks.get(pks.size() - 1));
				
				AsiaPl main = null;
				float hours = 0;
				AsiaPl temp = null;

				for (AsiaPl pk : pks) {
					if (temp != null) {
						Date thisDt = pk.getPkDate();
						Date lastDt = temp.getPkDate();
						
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

					temp = pk;
				}
				
				if (MatchUtil.getDiffHours(new Date(), temp.getPkDate()) >= hours) {
					main = temp;
				}

				pkMatrices.setMainPk(main);
				return pkMatrices;
			}
		}

		return null;
	}

	@Override
	public void calucate(PankouMatrices Result, OFNMatchData matchData) {
		// TODO Auto-generated method stub
		
	}

}
