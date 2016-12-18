package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class MatchState implements CalculateResult, MatchData {

	@Override
	public String toString() {
		return "MatchState [hostState6=" + hostState6 + ", guestState6="
				+ guestState6 + ", hostState10=" + hostState10
				+ ", guestState10=" + guestState10 + ", hostAttackToGuest="
				+ hostAttackToGuest + ", guestAttackToHost="
				+ guestAttackToHost + ", hostAttackVariationToGuest="
				+ hostAttackVariationToGuest + ", guestAttackVariationToHost="
				+ guestAttackVariationToHost + ", calculatePk=" + calculatePk
				+ "]";
	}

	public LatestMatchMatrices getHostState6() {
		return hostState6;
	}

	public void setHostState6(LatestMatchMatrices hostState6) {
		this.hostState6 = hostState6;
	}

	public LatestMatchMatrices getGuestState6() {
		return guestState6;
	}

	public void setGuestState6(LatestMatchMatrices guestState6) {
		this.guestState6 = guestState6;
	}

	public LatestMatchMatrices getHostState10() {
		return hostState10;
	}

	public void setHostState10(LatestMatchMatrices hostState10) {
		this.hostState10 = hostState10;
	}

	public LatestMatchMatrices getGuestState10() {
		return guestState10;
	}

	public void setGuestState10(LatestMatchMatrices guestState10) {
		this.guestState10 = guestState10;
	}

	public Float getCalculatePk() {
		return calculatePk;
	}

	public void setCalculatePk(Float calculatePk) {
		this.calculatePk = calculatePk;
	}
	

	public Float getHostAttackToGuest() {
		return hostAttackToGuest;
	}

	public void setHostAttackToGuest(Float hostAttackToGuest) {
		this.hostAttackToGuest = hostAttackToGuest;
	}

	public Float getGuestAttackToHost() {
		return guestAttackToHost;
	}

	public void setGuestAttackToHost(Float guestAttackToHost) {
		this.guestAttackToHost = guestAttackToHost;
	}

	public Float getHostAttackVariationToGuest() {
		return hostAttackVariationToGuest;
	}

	public void setHostAttackVariationToGuest(Float hostAttackVariationToGuest) {
		this.hostAttackVariationToGuest = hostAttackVariationToGuest;
	}

	public Float getGuestAttackVariationToHost() {
		return guestAttackVariationToHost;
	}

	public void setGuestAttackVariationToHost(Float guestAttackVariationToHost) {
		this.guestAttackVariationToHost = guestAttackVariationToHost;
	}


	public Float getHotPoint() {
		return hotPoint;
	}

	public void setHotPoint(Float hotPoint) {
		this.hotPoint = hotPoint;
	}


	private LatestMatchMatrices hostState6;
	private LatestMatchMatrices guestState6;
	private LatestMatchMatrices hostState10;
	private LatestMatchMatrices guestState10;
	private Float hostAttackToGuest;
	private Float guestAttackToHost;
	private Float hostAttackVariationToGuest;
	private Float guestAttackVariationToHost;
	private Float calculatePk;
	private Float hotPoint;
	
	public static class LatestMatchMatrices {

		@Override
		public String toString() {
			return "LatestMatchMatrices [winRate=" + winRate + ", winDrawRate="
					+ winDrawRate + ", winPkRate=" + winPkRate
					+ ", winDrawPkRate=" + winDrawPkRate + ", matchGoal="
					+ matchGoal + ", matchMiss=" + matchMiss + ", point="
					+ point + ", gVariation=" + gVariation + ", mVariation="
					+ mVariation + "]";
		}

		public Float getWinRate() {
			return winRate;
		}
		public void setWinRate(Float winRate) {
			this.winRate = winRate;
		}
		public Float getWinDrawRate() {
			return winDrawRate;
		}
		public void setWinDrawRate(Float winDrawRate) {
			this.winDrawRate = winDrawRate;
		}
		public Float getWinPkRate() {
			return winPkRate;
		}
		public void setWinPkRate(Float winPkRate) {
			this.winPkRate = winPkRate;
		}
		public Float getMatchGoal() {
			return matchGoal;
		}
		public void setMatchGoal(Float matchGoal) {
			this.matchGoal = matchGoal;
		}
		public Float getMatchMiss() {
			return matchMiss;
		}
		public void setMatchMiss(Float matchMiss) {
			this.matchMiss = matchMiss;
		}

		public Float getWinDrawPkRate() {
			return winDrawPkRate;
		}
		public void setWinDrawPkRate(Float winDrawPkRate) {
			this.winDrawPkRate = winDrawPkRate;
		}

		public Float getPoint() {
			return point;
		}

		public void setPoint(Float point) {
			this.point = point;
		}

		public Float getgVariation() {
			return gVariation;
		}

		public void setgVariation(Float gVariation) {
			this.gVariation = gVariation;
		}

		public Float getmVariation() {
			return mVariation;
		}

		public void setmVariation(Float mVariation) {
			this.mVariation = mVariation;
		}

		private Float winRate;
		private Float winDrawRate;
		private Float winPkRate;
		private Float winDrawPkRate;
		private Float matchGoal;
		private Float matchMiss;
		private Float point;
		private Float gVariation;                // goal
		private Float mVariation;                // miss
	}
}
