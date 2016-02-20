package com.roy.football.match.OFN.response;

import com.google.gson.annotations.SerializedName;

public class ClubDatas {

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

	private ClubData hostData;
	private ClubData guestData;
	
	public static class ClubData {

		public Integer getAllNum() {
			return homeNum + awayNum;
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
			return homeWin + awayWin;
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
			return homeDraw + awayDraw;
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
			return homeLose + awayLose;
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
			return homeGoal + awayGoal;
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
			return homeMiss + awayMiss;
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
			return getHomeNet() + getAwayNet();
		}
		public void setAllNet(Integer allNet) {
			this.allNet = allNet;
		}
		public Integer getHomeNet() {
			return homeGoal -homeMiss;
		}
		public void setHomeNet(Integer homeNet) {
			this.homeNet = homeNet;
		}
		public Integer getAwayNet() {
			return awayGoal -awayMiss;
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
		
		@SerializedName("all")
		private Integer allPm;
		@SerializedName("hpm")
		private Integer homePm;
		@SerializedName("apm")
		private Integer awayPm;
	}
	
}
