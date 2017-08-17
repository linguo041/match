package com.roy.football.match.okooo;

import lombok.Data;

@Data
public class MatchExchangeData {

	public boolean hasExchangeData (ExchangeType type, boolean gain) {
		if (type == ExchangeType.jc) {
			return gain
					? this.jcWinGain != null && this.jcDrawGain != null && this.jcLoseGain != null
					: this.jcWinExchange != null && this.jcDrawExchange != null && this.jcLoseExchange != null;
		} else {
			return gain
					? this.bfWinGain != null && this.bfDrawGain != null && this.bfLoseGain != null
					: this.bfWinExchange != null && this.bfDrawExchange != null && this.bfLoseExchange != null;
		}
		
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
	
	public static enum ExchangeType {
		jc, bf
	}
	
}
