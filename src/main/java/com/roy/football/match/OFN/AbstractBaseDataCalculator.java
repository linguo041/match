package com.roy.football.match.OFN;

public class AbstractBaseDataCalculator {

	protected float getPkWeightByHours (float lastDtToMatch, float thisDtToMatch) {
		float lastDtWeight = lastDtToMatch > 20 ? 0.5f : (1f - lastDtToMatch / 40);
		float thisDtWeight = thisDtToMatch > 20 ? 0.5f : (1f - thisDtToMatch / 40);
		
		lastDtToMatch = lastDtToMatch > 20 ? 20 : lastDtToMatch;
		thisDtToMatch = thisDtToMatch > 20 ? 20 : thisDtToMatch;

		return 0.5f * (lastDtWeight + thisDtWeight) * (lastDtToMatch - thisDtToMatch);
	}
}
