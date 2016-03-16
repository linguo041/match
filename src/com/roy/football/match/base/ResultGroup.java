package com.roy.football.match.base;

public enum ResultGroup {
	Three(3), One(1), Zero(0);
	
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
