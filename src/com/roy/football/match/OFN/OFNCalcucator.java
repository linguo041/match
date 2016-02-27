package com.roy.football.match.OFN;

import com.roy.football.match.OFN.response.OFNMatchData;
import com.roy.football.match.OFN.statics.matrices.ClubMatrices;
import com.roy.football.match.OFN.statics.matrices.OFNCalculateResult;
import com.roy.football.match.process.Calculator;

public class OFNCalcucator implements Calculator<OFNCalculateResult, OFNMatchData> {
	private final static BaseDataCalculator baseCalculator = new BaseDataCalculator();
	private final static BaseMatrixCalculator baseMatrixCalculator = new BaseMatrixCalculator();

	@Override
	public OFNCalculateResult calucate(OFNMatchData matchData) {
		
		if (matchData == null) {
			return null;
		}
		
		OFNCalculateResult calResult = new OFNCalculateResult();
		
		ClubMatrices matrices = baseCalculator.calucate(matchData);
		calResult.setClubMatrices(matrices);
		
		baseMatrixCalculator.calucate(calResult);

		return calResult;
	}

}
