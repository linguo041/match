package com.roy.football.match.OFN;

public class AbstractBaseDataCalculator {

	protected float getPkWeightByHours (float lastDtToMatch, float thisDtToMatch) {
		float lastDtWeight = lastDtToMatch > 24 ? 0.7f : (1f - 0.1f * lastDtToMatch / 8);
		float thisDtWeight = thisDtToMatch > 24 ? 0.7f : (1f - 0.1f * thisDtToMatch / 8);
		
		lastDtToMatch = lastDtToMatch > 24 ? 24 : lastDtToMatch;
		thisDtToMatch = thisDtToMatch > 24 ? 24 : thisDtToMatch;

		return 0.5f * (lastDtWeight + thisDtWeight) * (lastDtToMatch - thisDtToMatch);
	}
}
