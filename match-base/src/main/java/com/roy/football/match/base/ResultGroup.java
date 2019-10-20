package com.roy.football.match.base;

public enum ResultGroup {
	AboveThree(5), Three(3), One(1), Zero(0), BelowZero(-5), RangThree(5), RangZero(-5);
	
	@Override
	public String toString() {
		return getNum() + "";
	}

	ResultGroup(Integer num) {
		this.num = num;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	private Integer num;
}
