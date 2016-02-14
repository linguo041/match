package com.roy.football.match;

public class Peilv {
	
	
	public Peilv(Float winPay, Float drawPay, Float lostPay) {
		this.winPay = winPay;
		this.drawPay = drawPay;
		this.lostPay = lostPay;
	}

	public Float getWinPay() {
		return winPay;
	}
	public void setWinPay(Float winPay) {
		this.winPay = winPay;
	}
	public Float getDrawPay() {
		return drawPay;
	}
	public void setDrawPay(Float drawPay) {
		this.drawPay = drawPay;
	}
	public Float getLostPay() {
		return lostPay;
	}
	public void setLostPay(Float lostPay) {
		this.lostPay = lostPay;
	}
	private Float winPay;
	private Float drawPay;
	private Float lostPay;
}
