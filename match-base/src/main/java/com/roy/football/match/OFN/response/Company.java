package com.roy.football.match.OFN.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Company {
	Jincai(10000, 1L, 1585L, PaidLevel.Low), William (451, 293L, 21L, PaidLevel.High),
	Ladbrokes(449, 2L, 26L, PaidLevel.High), Interwetten(211, 4L, 140L, PaidLevel.Middle),
	Aomen (442, 5L, 196L, PaidLevel.Middle), YiShenBo(454, 9L, 184L, PaidLevel.Middle), SNAI(325, 8L, 174L, PaidLevel.High),
	Sweden(370, 225L, 298L, PaidLevel.High);
	
	Company (long cid, PaidLevel paidLevel) {
		this.companyId = cid;
		this.paidLevel = paidLevel;
	}
	
	Company (long cid, Long fmCid, Long acCid, PaidLevel paidLevel) {
		this.companyId = cid;
		this.fmCompanyId = fmCid;
		this.acCompanyId = acCid;
		this.paidLevel = paidLevel;
	}
	
	public String toString () {
		return this.name();
	}

	public static Company companyIdOf (Long companyId) {
		if (companyId != null) {
			for (Company comp : Company.values()) {
				if (companyId == comp.getCompanyId()) {
					return comp;
				}
			}
		}
		
		return null;
	}

	private Long companyId;
	private Long fmCompanyId;
	private Long acCompanyId;
	private PaidLevel paidLevel;
	
	public static enum PaidLevel {
		High, Middle, Low;
	}
}
