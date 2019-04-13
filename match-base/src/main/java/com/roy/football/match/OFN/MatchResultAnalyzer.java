package com.roy.football.match.OFN;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.roy.football.match.OFN.response.MatchResultAnalyzed;
import com.roy.football.match.jpa.entities.calculation.EMatchResultDetail;
import com.roy.football.match.jpa.service.MatchComplexQueryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchResultAnalyzer {
	
	@Autowired
	private MatchComplexQueryService matchComplexQueryService;
	
	public MatchResultAnalyzed calMatchResultSummary (Long teamId, int hostCnt, int guestCnt) {
		List<EMatchResultDetail> hostEmrs = matchComplexQueryService.findLatestMatchResult(teamId, true, hostCnt);
		List<EMatchResultDetail> guestEmrs = matchComplexQueryService.findLatestMatchResult(teamId, false, guestCnt);
		MatchResultAnalyzed hostEma = null;
		MatchResultAnalyzed guestEma = null;
		
		if (!hostEmrs.isEmpty() ) {
			hostEma = hostEmrs.stream()
			.reduce(new AveragerAccumulator(), AveragerAccumulator::accept, AveragerAccumulator::combine)
			.average();
		}
		if (!guestEmrs.isEmpty() ) {
			guestEma = guestEmrs.stream()
			.reduce(new AveragerAccumulator(), AveragerAccumulator::accept, AveragerAccumulator::combine)
			.average();
		}
		
		if (hostEma != null && guestEma != null) {
			return mergeMatchResultAnalyzed(hostEma, guestEma, hostCnt, guestCnt);
		} else if (hostEma != null) {
			return hostEma;
		} else {
			return guestEma;
		}
	}
	
	private MatchResultAnalyzed mergeMatchResultAnalyzed (MatchResultAnalyzed host, MatchResultAnalyzed guest, int hostCnt, int guestCnt) {
		MatchResultAnalyzed merged = new MatchResultAnalyzed();
		merged.setHostScore((host.getHostScore() * hostCnt + guest.getGuestScore() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestScore((host.getGuestScore() * hostCnt + guest.getHostScore() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostShot((host.getHostShot() * hostCnt + guest.getGuestShot() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestShot((host.getGuestShot() * hostCnt + guest.getHostShot() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostShotOnTarget((host.getHostShotOnTarget() * hostCnt + guest.getGuestShotOnTarget() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestShotOnTarget((host.getGuestShotOnTarget() * hostCnt + guest.getHostShotOnTarget() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostFault((host.getHostFault() * hostCnt + guest.getGuestFault() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestFault((host.getGuestFault() * hostCnt + guest.getHostFault() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostCorner((host.getHostCorner() * hostCnt + guest.getGuestCorner() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestCorner((host.getGuestCorner() * hostCnt + guest.getHostCorner() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostOffside((host.getHostOffside() * hostCnt + guest.getGuestOffside() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestOffside((host.getGuestOffside() * hostCnt + guest.getHostOffside() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostYellowCard((host.getHostYellowCard() * hostCnt + guest.getGuestYellowCard() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestYellowCard((host.getGuestYellowCard() * hostCnt + guest.getHostYellowCard() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostTime((host.getHostTime() * hostCnt + guest.getGuestTime() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestTime((host.getGuestTime() * hostCnt + guest.getHostTime() * guestCnt) / (hostCnt + guestCnt));
		merged.setHostSave((host.getHostSave() * hostCnt + guest.getGuestSave() * guestCnt) / (hostCnt + guestCnt));
		merged.setGuestSave((host.getGuestSave() * hostCnt + guest.getHostSave() * guestCnt) / (hostCnt + guestCnt));
		return merged;
	}
	
	static class AveragerAccumulator {
		private int hostScore;
		private int guestScore;
		private int hostShot;
		private int guestShot;
		private int hostShotOnTarget;
		private int guestShotOnTarget;
		private int hostFault;
		private int guestFault;
		private int hostCorner;
		private int guestCorner;
		private int hostOffside;
		private int guestOffside;
		private int hostYellowCard;
		private int guestYellowCard;
		private float hostTime;
		private float guestTime;
		private int hostSave;
		private int guestSave;
		
		private int scoreC;
		private int shotC;
		private int shotOnTargetC;
		private int faultC;
		private int cornerC;
		private int offsideC;
		private int yellowCardC;
		private int timeC;
		private int saveC;

	    public AveragerAccumulator() {
	    }

	    public MatchResultAnalyzed average() {
	    	MatchResultAnalyzed res = new MatchResultAnalyzed();
	    	res.setHostScore(1.0f * this.hostScore / this.scoreC);
	    	res.setGuestScore(1.0f * this.guestScore / this.scoreC);
	    	res.setHostShot(1.0f * this.hostShot / this.shotC);
	    	res.setGuestShot(1.0f * this.guestShot / this.shotC);
	    	res.setHostShotOnTarget(1.0f * this.hostShotOnTarget / this.shotOnTargetC);
	    	res.setGuestShotOnTarget(1.0f * this.guestShotOnTarget / this.shotOnTargetC);
	    	res.setHostFault(1.0f * this.hostFault / this.faultC);
	    	res.setGuestFault(1.0f * this.guestFault / this.faultC);
	    	res.setHostCorner(1.0f * this.hostCorner / this.cornerC);
	    	res.setGuestCorner(1.0f * this.guestCorner / this.cornerC);
	    	res.setHostOffside(1.0f * this.hostOffside / this.offsideC);
	    	res.setGuestOffside(1.0f * this.guestOffside / this.offsideC);
	    	res.setHostYellowCard(1.0f * this.hostYellowCard / this.yellowCardC);
	    	res.setGuestYellowCard(1.0f * this.guestYellowCard / this.yellowCardC);
	    	res.setHostTime(1.0f * this.hostTime / this.timeC);
	    	res.setGuestTime(1.0f * this.guestTime / this.timeC);
	    	res.setHostSave(1.0f * this.hostSave / this.saveC);
	    	res.setGuestSave(1.0f * this.guestSave / this.saveC);
	        return res;
	    }

	    public AveragerAccumulator accept(EMatchResultDetail mr) {
	    	if (mr != null) {
	    		if (mr.getHostScore() != null) {
	    			this.hostScore += mr.getHostScore();
	    			this.guestScore += mr.getGuestScore();
	    			this.scoreC++;
	    		}
	    		if (mr.getHostShotOnTarget() != null
	    				&& (mr.getHostShotOnTarget() > 0 || mr.getGuestShotOnTarget() > 0)) {
	    			this.hostShotOnTarget += mr.getHostShotOnTarget();
	    			this.guestShotOnTarget += mr.getGuestShotOnTarget();
	    			this.shotOnTargetC++;
	    		}
	    		if (mr.getHostShot() != null
	    				&& (mr.getHostShot() > 0 || mr.getGuestShot() > 0)) {
	    			this.hostShot += mr.getHostShot();
	    			this.guestShot += mr.getGuestShot();
	    			this.shotC++;
	    		}
	    		if (mr.getHostFault() != null
	    				&& (mr.getHostFault() > 0 || mr.getGuestFault() > 0)) {
	    			this.hostFault += mr.getHostFault();
	    			this.guestFault += mr.getGuestFault();
	    			this.faultC++;
	    		}
	    		if (mr.getHostCorner() != null
	    				&& (mr.getHostCorner() > 0 || mr.getGuestCorner() > 0)) {
	    			this.hostCorner += mr.getHostCorner();
	    			this.guestCorner += mr.getGuestCorner();
	    			this.cornerC++;
	    		}
	    		if (mr.getHostOffside() != null
	    				&& (mr.getHostOffside() > 0 || mr.getGuestOffside() > 0)) {
	    			this.hostOffside += mr.getHostOffside();
	    			this.guestOffside += mr.getGuestOffside();
	    			this.offsideC++;
	    		}
	    		if (mr.getHostYellowCard() != null
	    				&& (mr.getHostYellowCard() > 0 || mr.getGuestYellowCard() > 0)) {
	    			this.hostYellowCard += mr.getHostYellowCard();
	    			this.guestYellowCard += mr.getGuestYellowCard();
	    			this.yellowCardC++;
	    		}
	    		if (mr.getHostTime() != null && mr.getHostTime() > 0.0f) {
	    			this.hostTime += mr.getHostTime();
	    			this.guestTime += mr.getGuestTime();
	    			this.timeC++;
	    		}
	    		if (mr.getHostSave() != null
	    				&& (mr.getHostSave() > 0 || mr.getGuestSave() > 0)) {
	    			this.hostSave += mr.getHostSave();
	    			this.guestSave += mr.getGuestSave();
	    			this.saveC++;
	    		}
	    	}
	    	
	    	return this;
	    }

	    public AveragerAccumulator combine(AveragerAccumulator other) {
	    	this.hostScore += other.hostScore;
			this.guestScore += other.guestScore;
			this.scoreC += other.scoreC;
			this.hostShotOnTarget += other.hostShotOnTarget;
			this.guestShotOnTarget += other.guestShotOnTarget;
			this.shotOnTargetC += other.shotOnTargetC;
			this.hostShot += other.hostShot;
			this.guestShot += other.guestShot;
			this.shotC += other.shotC;
			this.hostFault += other.hostFault;
			this.guestFault += other.guestFault;
			this.faultC += other.faultC;
			this.hostCorner += other.hostCorner;
			this.guestCorner += other.guestCorner;
			this.cornerC += other.cornerC;
			this.hostOffside += other.hostOffside;
			this.guestOffside += other.guestOffside;
			this.offsideC += other.offsideC;
			this.hostYellowCard += other.hostYellowCard;
			this.guestYellowCard += other.guestYellowCard;
			this.yellowCardC += other.yellowCardC;
			this.hostTime += other.hostTime;
			this.guestTime += other.guestTime;
			this.timeC += other.timeC;
			this.hostSave += other.hostSave;
			this.guestSave += other.guestSave;
			this.saveC += other.saveC;
			
			return this;
	    }
	}
}
