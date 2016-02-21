package com.roy.football.match.OFN.response;

public enum Company {
	William (442l), Aomen (123l);
	
	Company (long cid) {
		setCompanyId(cid);
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	private Long companyId;
}
