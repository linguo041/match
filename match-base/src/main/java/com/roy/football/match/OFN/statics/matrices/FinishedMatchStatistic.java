package com.roy.football.match.OFN.statics.matrices;

import lombok.Data;

@Data
public class FinishedMatchStatistic {
	private float control;  		// time is more and pass is more
	private float controlQuality;	// control / (total fault, total yellow card), qlt is large, control is strong
	private float attack;			// shot, shotOnTarget, score
	private float attackQuality;    // attack / (guest fault, guest yellow card), qlt is large, attach is strong
	private float defend;			// shotted, shottedOnTarget scored are less
	private float defendDuality;   	// defend + 1/(host fault, host yellow card), qlt is small, denfend is strong  ?????
	private float quality;			// host fault, host yellow card, total fault
}
