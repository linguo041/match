package com.roy.football.match.OFN.response;

public enum Company {
	Jincai(10000, PaidLevel.Low), William (451, PaidLevel.High), Ladbrokes(449, PaidLevel.High), Interwetten(211, PaidLevel.Middle),
	Aomen (442, PaidLevel.Middle), YiShenBo(454, PaidLevel.Middle), SNAI(325, PaidLevel.High), Sweden(370, PaidLevel.High);
	
	Company (long cid, PaidLevel paidLevel) {
		setCompanyId(cid);
		setPaidLevel(paidLevel);
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public PaidLevel getPaidLevel() {
		return paidLevel;
	}

	public void setPaidLevel(PaidLevel paidLevel) {
		this.paidLevel = paidLevel;
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
	private PaidLevel paidLevel;
	
	public static enum PaidLevel {
		High, Middle, Low;
	}
}
