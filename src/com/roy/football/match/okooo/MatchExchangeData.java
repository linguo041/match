package com.roy.football.match.okooo;

public class MatchExchangeData {

	@Override
	public String toString() {
		return "MatchExchangeData [bfWinExchange=" + bfWinExchange
				+ ", bfDrawExchange=" + bfDrawExchange + ", bfLoseExchange="
				+ bfLoseExchange + ", bfWinExgRt=" + bfWinExgRt
				+ ", bfDrawExgRt=" + bfDrawExgRt + ", bfLoseExgRt="
				+ bfLoseExgRt + ", bfWinGain=" + bfWinGain + ", bfDrawGain="
				+ bfDrawGain + ", bfLoseGain=" + bfLoseGain
				+ ", jcWinExchange=" + jcWinExchange + ", jcDrawExchange="
				+ jcDrawExchange + ", jcLoseExchange=" + jcLoseExchange
				+ ", jcWinExgRt=" + jcWinExgRt + ", jcDrawExgRt=" + jcDrawExgRt
				+ ", jcLoseExgRt=" + jcLoseExgRt + ", jcWinGain=" + jcWinGain
				+ ", jcDrawGain=" + jcDrawGain + ", jcLoseGain=" + jcLoseGain
				+ "]";
	}

	public Long getBfWinExchange() {
		return bfWinExchange;
	}
	public void setBfWinExchange(Long bfWinExchange) {
		this.bfWinExchange = bfWinExchange;
	}
	public Long getBfDrawExchange() {
		return bfDrawExchange;
	}
	public void setBfDrawExchange(Long bfDrawExchange) {
		this.bfDrawExchange = bfDrawExchange;
	}
	public Long getBfLoseExchange() {
		return bfLoseExchange;
	}
	public void setBfLoseExchange(Long bfLoseExchange) {
		this.bfLoseExchange = bfLoseExchange;
	}
	public Float getBfWinExgRt() {
		return bfWinExgRt;
	}
	public void setBfWinExgRt(Float bfWinExgRt) {
		this.bfWinExgRt = bfWinExgRt;
	}
	public Float getBfDrawExgRt() {
		return bfDrawExgRt;
	}
	public void setBfDrawExgRt(Float bfDrawExgRt) {
		this.bfDrawExgRt = bfDrawExgRt;
	}
	public Float getBfLoseExgRt() {
		return bfLoseExgRt;
	}
	public void setBfLoseExgRt(Float bfLoseExgRt) {
		this.bfLoseExgRt = bfLoseExgRt;
	}
	public Integer getBfWinGain() {
		return bfWinGain;
	}
	public void setBfWinGain(Integer bfWinGain) {
		this.bfWinGain = bfWinGain;
	}
	public Integer getBfDrawGain() {
		return bfDrawGain;
	}
	public void setBfDrawGain(Integer bfDrawGain) {
		this.bfDrawGain = bfDrawGain;
	}
	public Integer getBfLoseGain() {
		return bfLoseGain;
	}
	public void setBfLoseGain(Integer bfLoseGain) {
		this.bfLoseGain = bfLoseGain;
	}
	public Long getJcWinExchange() {
		return jcWinExchange;
	}
	public void setJcWinExchange(Long jcWinExchange) {
		this.jcWinExchange = jcWinExchange;
	}
	public Long getJcDrawExchange() {
		return jcDrawExchange;
	}
	public void setJcDrawExchange(Long jcDrawExchange) {
		this.jcDrawExchange = jcDrawExchange;
	}
	public Long getJcLoseExchange() {
		return jcLoseExchange;
	}
	public void setJcLoseExchange(Long jcLoseExchange) {
		this.jcLoseExchange = jcLoseExchange;
	}
	public Float getJcWinExgRt() {
		return jcWinExgRt;
	}
	public void setJcWinExgRt(Float jcWinExgRt) {
		this.jcWinExgRt = jcWinExgRt;
	}
	public Float getJcDrawExgRt() {
		return jcDrawExgRt;
	}
	public void setJcDrawExgRt(Float jcDrawExgRt) {
		this.jcDrawExgRt = jcDrawExgRt;
	}
	public Float getJcLoseExgRt() {
		return jcLoseExgRt;
	}
	public void setJcLoseExgRt(Float jcLoseExgRt) {
		this.jcLoseExgRt = jcLoseExgRt;
	}
	public Integer getJcWinGain() {
		return jcWinGain;
	}
	public void setJcWinGain(Integer jcWinGain) {
		this.jcWinGain = jcWinGain;
	}
	public Integer getJcDrawGain() {
		return jcDrawGain;
	}
	public void setJcDrawGain(Integer jcDrawGain) {
		this.jcDrawGain = jcDrawGain;
	}
	public Integer getJcLoseGain() {
		return jcLoseGain;
	}
	public void setJcLoseGain(Integer jcLoseGain) {
		this.jcLoseGain = jcLoseGain;
	}
	public Long getJcTotalExchange () {
		return this.jcWinExchange + this.jcDrawExchange + this.jcLoseExchange;
	}

	private Long bfWinExchange;
	private Long bfDrawExchange;
	private Long bfLoseExchange;
	private Float bfWinExgRt;
	private Float bfDrawExgRt;
	private Float bfLoseExgRt;
	private Integer bfWinGain;
	private Integer bfDrawGain;
	private Integer bfLoseGain;
	
	private Long jcWinExchange;
	private Long jcDrawExchange;
	private Long jcLoseExchange;
	private Float jcWinExgRt;
	private Float jcDrawExgRt;
	private Float jcLoseExgRt;
	private Integer jcWinGain;
	private Integer jcDrawGain;
	private Integer jcLoseGain;
	
}
