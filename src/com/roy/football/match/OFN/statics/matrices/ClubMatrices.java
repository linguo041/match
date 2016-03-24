package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.process.CalculateResult;

public class ClubMatrices implements CalculateResult, MatchData{

	@Override
	public String toString() {
		return "ClubMatrices [hostAllMatrix=" + hostAllMatrix
				+ ", hostHomeMatrix=" + hostHomeMatrix + ", hostAwayMatrix="
				+ hostAwayMatrix + ", guestAllMatrix=" + guestAllMatrix
				+ ", guestHomeMatrix=" + guestHomeMatrix + ", guestAwayMatrix="
				+ guestAwayMatrix + "]";
	}

	public ClubMatrix getHostAllMatrix() {
		return hostAllMatrix;
	}

	public void setHostAllMatrix(ClubMatrix hostAllMatrix) {
		this.hostAllMatrix = hostAllMatrix;
	}

	public ClubMatrix getHostHomeMatrix() {
		return hostHomeMatrix;
	}

	public void setHostHomeMatrix(ClubMatrix hostHomeMatrix) {
		this.hostHomeMatrix = hostHomeMatrix;
	}

	public ClubMatrix getHostAwayMatrix() {
		return hostAwayMatrix;
	}

	public void setHostAwayMatrix(ClubMatrix hostAwayMatrix) {
		this.hostAwayMatrix = hostAwayMatrix;
	}

	public ClubMatrix getGuestAllMatrix() {
		return guestAllMatrix;
	}

	public void setGuestAllMatrix(ClubMatrix guestAllMatrix) {
		this.guestAllMatrix = guestAllMatrix;
	}

	public ClubMatrix getGuestHomeMatrix() {
		return guestHomeMatrix;
	}

	public void setGuestHomeMatrix(ClubMatrix guestHomeMatrix) {
		this.guestHomeMatrix = guestHomeMatrix;
	}

	public ClubMatrix getGuestAwayMatrix() {
		return guestAwayMatrix;
	}

	public void setGuestAwayMatrix(ClubMatrix guestAwayMatrix) {
		this.guestAwayMatrix = guestAwayMatrix;
	}

	private ClubMatrix hostAllMatrix;
	private ClubMatrix hostHomeMatrix;
	private ClubMatrix hostAwayMatrix;
	private ClubMatrix guestAllMatrix;
	private ClubMatrix guestHomeMatrix;
	private ClubMatrix guestAwayMatrix;
	
	public static class ClubMatrix {

		@Override
		public String toString() {
			return "ClubMatrix [num=" + num + ", winRt=" + winRt
					+ ", winDrawRt=" + winDrawRt + ", drawLoseRt=" + drawLoseRt
					+ ", goals=" + goals + ", misses=" + misses + ", winGoals="
					+ winGoals + ", winLoseDiff=" + winLoseDiff + ", pm=" + pm
					+ ", point=" + point + "]";
		}

		public Float getWinRt() {
			return winRt;
		}
		public void setWinRt(Float winRt) {
			this.winRt = winRt;
		}
		public Float getWinDrawRt() {
			return winDrawRt;
		}
		public void setWinDrawRt(Float winDrawRt) {
			this.winDrawRt = winDrawRt;
		}
		public Integer getWinGoals() {
			return winGoals;
		}
		public void setWinGoals(Integer winGoals) {
			this.winGoals = winGoals;
		}
		public Integer getPm() {
			return pm;
		}
		public void setPm(Integer pm) {
			this.pm = pm;
		}
		public Float getDrawLoseRt() {
			return drawLoseRt;
		}
		public void setDrawLoseRt(Float drawLoseRt) {
			this.drawLoseRt = drawLoseRt;
		}
		public Integer getWinLoseDiff() {
			return winLoseDiff;
		}

		public void setWinLoseDiff(Integer winLoseDiff) {
			this.winLoseDiff = winLoseDiff;
		}
		public Integer getGoals() {
			return goals;
		}

		public void setGoals(Integer goals) {
			this.goals = goals;
		}

		
		public Integer getNum() {
			return num;
		}

		public void setNum(Integer num) {
			this.num = num;
		}

		public Integer getMisses() {
			return misses;
		}
		public void setMisses(Integer misses) {
			this.misses = misses;
		}

		public Integer getPoint() {
			return point;
		}
		public void setPoint(Integer point) {
			this.point = point;
		}

		private Integer num;
		private Float winRt;
		private Float winDrawRt;
		private Float drawLoseRt;
		private Integer goals;
		private Integer misses;
		private Integer winGoals;
		private Integer winLoseDiff;
		private Integer pm;
		private Integer point;
	}
}
