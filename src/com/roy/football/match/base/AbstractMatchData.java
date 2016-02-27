package com.roy.football.match.base;

import com.roy.football.match.process.CalculateResult;

public class AbstractMatchData <T extends CalculateResult> {

	private T calculateResult;

	public T getCalculateResult() {
		return calculateResult;
	}

	public void setCalculateResult(T calculateResult) {
		this.calculateResult = calculateResult;
	}
}
