package com.roy.football.match.OFN.response;

public enum Company {
	William (451l), Ladbrokes(449), Interwetten(211), Aomen (442l), YiShenBo(454l);
	
	Company (long cid) {
		setCompanyId(cid);
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
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
}
