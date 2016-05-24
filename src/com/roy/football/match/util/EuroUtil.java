package com.roy.football.match.util;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.base.League;

public class EuroUtil {
	public static EuroMatrix getMainEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		if (company != null) {
			switch (company) {
				case Aomen:
					return euroMatrices.getAomenMatrix();
				case SNAI:
					return euroMatrices.getSnaiMatrix();
				case Sweden:
					return euroMatrices.getSwedenMatrix();
				default:
					return euroMatrices.getWilliamMatrix();
			}
		}

		return euroMatrices.getWilliamMatrix();
	}

	public static EuroMatrix getHighPaidEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		if (company != null) {
			switch (company) {
				case SNAI:
					return euroMatrices.getSnaiMatrix();
				case Sweden:
					return euroMatrices.getSwedenMatrix();
				default:
					return euroMatrices.getWilliamMatrix();
			}
		}

		return euroMatrices.getWilliamMatrix();
	}
	
	public static boolean isAomenTheMajor (League league) {
		Company company = league.getMajorCompany();
		if (company != null && company == Company.Aomen) {
			return true;
		}
		return false;
	}
}
