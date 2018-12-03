package com.roy.football.match.OFN;

public class AbstractBaseDataCalculator {

	protected float getPkWeightByHours (float lastDtToMatch, float thisDtToMatch) {
//		float lastDtWeight = lastDtToMatch > 20 ? 0.5f : (1f - lastDtToMatch / 40);
//		float thisDtWeight = thisDtToMatch > 20 ? 0.5f : (1f - thisDtToMatch / 40);
		
		float lastDtWeight = weightFactor(lastDtToMatch);
		float thisDtWeight = weightFactor(thisDtToMatch);
		
		lastDtToMatch = lastDtToMatch > 20 ? 20 : lastDtToMatch;
		thisDtToMatch = thisDtToMatch > 20 ? 20 : thisDtToMatch;

		return 0.5f * (lastDtWeight + thisDtWeight) * (lastDtToMatch - thisDtToMatch);
	}
	
	private float weightFactor (float timeToMatch) {

		if (timeToMatch > 20) {
			return 0.4f;
		} else if (timeToMatch > 15) {
			return 0.5f;
		} else if (timeToMatch > 10) {
			return 0.6f;
		} else if (timeToMatch > 7.5) {
			return 0.7f;
		} else if (timeToMatch > 5) {
			return 0.8f;
		} else if (timeToMatch > 3) {
			return 0.9f;
		} else {
			return 1;
		}
	}
}
