package com.roy.football.match.base;

public enum ResultGroup {
	Three("3"), One("1"), Zero("0"), ThreeOne("31"), OneZero("10"), ThreeZero("30");

	ResultGroup(String num) {
		this.num = num;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}
	
	private String num;
}
