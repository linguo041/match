package com.roy.football.match.OFN.response;

import com.google.gson.annotations.SerializedName;
import com.roy.football.match.base.MatchData;

public class ClubDatas implements MatchData {
	
	@Override
	public String toString() {
		return "ClubDatas [hostData=" + hostData + ", guestData=" + guestData
				+ "]";
	}

	public ClubData getHostData() {
		return hostData;
	}

	public void setHostData(ClubData hostData) {
		this.hostData = hostData;
	}

	public ClubData getGuestData() {
		return guestData;
	}

	public void setGuestData(ClubData guestData) {
		this.guestData = guestData;
	}

	@SerializedName("home")
	private ClubData hostData;
	@SerializedName("away")
	private ClubData guestData;
	
	public static class ClubData {

		@Override
		public String toString() {
			return "ClubData [allNum=" + getAllNum() + ", homeNum="
					+ getHomeNum() + ", awayNum=" + getAwayNum()
					+ ", allWin=" + getAllWin() + ", homeWin="
					+ getHomeWin() + ", awayWin=" + getAwayWin()
					+ ", allDraw=" + getAllDraw() + ", homeDraw="
					+ getHomeDraw() + ", awayDraw=" + getAwayDraw()
					+ ", allLose=" + getAllLose() + ", homeLose="
					+ getHomeLose() + ", awayLose=" + getAwayLose()
					+ ", allGoal=" + getAllGoal() + ", homeGoal="
					+ getHomeGoal() + ", awayGoal=" + getAwayGoal()
					+ ", allMiss=" + getAllMiss() + ", homeMiss="
					+ getHomeMiss() + ", awayMiss=" + getAwayMiss()
					+ ", allNet=" + getAllNet() + ", homeNet="
					+ getHomeNet() + ", awayNet=" + getAwayNet()
					+ ", allScore=" + getAllScore() + ", homeScore="
					+ getHomeScore() + ", awayScore=" + getAwayScore()
					+ ", pm=" + getPm() + "]";
		}

		public Integer getAllNum() {
			if (allNum == null && homeNum != null && awayNum != null) {
				allNum = homeNum + awayNum;
			}

			return allNum;
		}
		public void setAllNum(Integer allNum) {
			this.allNum = allNum;
		}
		public Integer getHomeNum() {
			return homeNum;
		}
		public void setHomeNum(Integer homeNum) {
			this.homeNum = homeNum;
		}
		public Integer getAwayNum() {
			return awayNum;
		}
		public void setAwayNum(Integer awayNum) {
			this.awayNum = awayNum;
		}
		public Integer getAllWin() {
			if (allWin == null && homeWin != null && awayWin != null) {
				allWin = homeWin + awayWin;
			}
			return allWin;
		}
		public void setAllWin(Integer allWin) {
			this.allWin = allWin;
		}
		public Integer getHomeWin() {
			return homeWin;
		}
		public void setHomeWin(Integer homeWin) {
			this.homeWin = homeWin;
		}
		public Integer getAwayWin() {
			return awayWin;
		}
		public void setAwayWin(Integer awayWin) {
			this.awayWin = awayWin;
		}
		public Integer getAllDraw() {
			if (allDraw == null && homeDraw != null && awayDraw != null) {
				allDraw = homeDraw + awayDraw;
			}
			return allDraw;
		}
		public void setAllDraw(Integer allDraw) {
			this.allDraw = allDraw;
		}
		public Integer getHomeDraw() {
			return homeDraw;
		}
		public void setHomeDraw(Integer homeDraw) {
			this.homeDraw = homeDraw;
		}
		public Integer getAwayDraw() {
			return awayDraw;
		}
		public void setAwayDraw(Integer awayDraw) {
			this.awayDraw = awayDraw;
		}
		public Integer getAllLose() {
			if (allLose == null && homeLose != null && awayLose != null) {
				allLose = homeLose + awayLose;
			}
			return allLose;
		}
		public void setAllLose(Integer allLose) {
			this.allLose = allLose;
		}
		public Integer getHomeLose() {
			return homeLose;
		}
		public void setHomeLose(Integer homeLose) {
			this.homeLose = homeLose;
		}
		public Integer getAwayLose() {
			return awayLose;
		}
		public void setAwayLose(Integer awayLose) {
			this.awayLose = awayLose;
		}
		public Integer getAllGoal() {
			if (allGoal == null && homeGoal != null && awayGoal != null) {
				allGoal = homeGoal + awayGoal;
			}
			return allGoal;
		}
		public void setAllGoal(Integer allGoal) {
			this.allGoal = allGoal;
		}
		public Integer getHomeGoal() {
			return homeGoal;
		}
		public void setHomeGoal(Integer homeGoal) {
			this.homeGoal = homeGoal;
		}
		public Integer getAwayGoal() {
			return awayGoal;
		}
		public void setAwayGoal(Integer awayGoal) {
			this.awayGoal = awayGoal;
		}
		public Integer getAllMiss() {
			if (allMiss == null && homeMiss != null && awayMiss != null) {
				allMiss = homeMiss + awayMiss;
			}
			return allMiss;
		}
		public void setAllMiss(Integer allMiss) {
			this.allMiss = allMiss;
		}
		public Integer getHomeMiss() {
			return homeMiss;
		}
		public void setHomeMiss(Integer homeMiss) {
			this.homeMiss = homeMiss;
		}
		public Integer getAwayMiss() {
			return awayMiss;
		}
		public void setAwayMiss(Integer awayMiss) {
			this.awayMiss = awayMiss;
		}
		public Integer getAllNet() {
			if (allNet == null && getHomeNet() != null && getAwayNet() != null) {
				allNet = getHomeNet() + getAwayNet();
			}
			return allNet;
		}
		public void setAllNet(Integer allNet) {
			this.allNet = allNet;
		}
		public Integer getHomeNet() {
			if (homeNet == null && homeGoal != null && homeMiss != null) {
				homeNet = homeGoal - homeMiss;
			}
			return homeNet;
		}
		public void setHomeNet(Integer homeNet) {
			this.homeNet = homeNet;
		}
		public Integer getAwayNet() {
			if (awayNet == null && awayGoal != null && awayMiss != null) {
				awayNet = awayGoal -awayMiss;
			}
			return awayNet;
		}
		public void setAwayNet(Integer awayNet) {
			this.awayNet = awayNet;
		}
		public Integer getAllScore() {
			return allScore;
		}
		public void setAllScore(Integer allScore) {
			this.allScore = allScore;
		}
		public Integer getHomeScore() {
			return homeScore;
		}
		public void setHomeScore(Integer homeScore) {
			this.homeScore = homeScore;
		}
		public Integer getAwayScore() {
			return awayScore;
		}
		public void setAwayScore(Integer awayScore) {
			this.awayScore = awayScore;
		}
		public PaiMing getPm() {
			return pm;
		}
		public void setPm(PaiMing pm) {
			this.pm = pm;
		}

		private Integer allNum;
		@SerializedName("hnum")
		private Integer homeNum;
		@SerializedName("anum")
		private Integer awayNum;
		
		private Integer allWin;
		@SerializedName("hw")
		private Integer homeWin;
		@SerializedName("aw")
		private Integer awayWin;
		
		private Integer allDraw;
		@SerializedName("hd")
		private Integer homeDraw;
		@SerializedName("ad")
		private Integer awayDraw;
		
		private Integer allLose;
		@SerializedName("hl")
		private Integer homeLose;
		@SerializedName("al")
		private Integer awayLose;
		
		private Integer allGoal;
		@SerializedName("hjq")
		private Integer homeGoal;
		@SerializedName("ajq")
		private Integer awayGoal;
		
		private Integer allMiss;
		@SerializedName("hsq")
		private Integer homeMiss;
		@SerializedName("asq")
		private Integer awayMiss;
		
		private Integer allNet;
		private Integer homeNet;
		private Integer awayNet;
		
		@SerializedName("score")
		private Integer allScore;
		@SerializedName("hscore")
		private Integer homeScore;
		@SerializedName("ascore")
		private Integer awayScore;
		
		private PaiMing pm;
	}
	
	public static class PaiMing {

		@Override
		public String toString() {
			return "PaiMing [allPm=" + allPm + ", homePm=" + homePm
					+ ", awayPm=" + awayPm + "]";
		}

		public Integer getAllPm() {
			return allPm;
		}
		public void setAllPm(Integer allPm) {
			this.allPm = allPm;
		}
		public Integer getHomePm() {
			return homePm;
		}
		public void setHomePm(Integer homePm) {
			this.homePm = homePm;
		}
		public Integer getAwayPm() {
			return awayPm;
		}
		public void setAwayPm(Integer awayPm) {
			this.awayPm = awayPm;
		}
		@SerializedName("all")
		private Integer allPm;
		@SerializedName("hpm")
		private Integer homePm;
		@SerializedName("apm")
		private Integer awayPm;
	}
}
