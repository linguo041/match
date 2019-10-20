package com.roy.football.match.util;

import java.util.Map;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.base.League;

public class EuroUtil {
	private static float[] JC_PAY_STD = new float[]{2.42f, 3.3f, 2.42f};
	private static float TOTAL_JC_PL_PAY = 8.14f;
	
	public static float getExtraEoruPl (EuroPl pl) {
		return pl.getEWin() + pl.getEDraw() + pl.getELose() - TOTAL_JC_PL_PAY;
	}
	
	public static EuroMatrix getMainEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		if (company != null) {
			switch (company) {
				case Aomen:
				case SNAI:
				case Sweden:
					break;
				default:
					company = Company.William;
			}
		}

		return companyEus.get(company);
	}

	public static EuroMatrix getHighPaidEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		Map<Company, EuroMatrix> companyEus = euroMatrices.getCompanyEus();
		if (company != null) {
			switch (company) {
				case SNAI:
				case Sweden:
					return companyEus.get(company);
				default:
					return companyEus.get(Company.William);
			}
		}

		return companyEus.get(Company.William);
	}
	
	public static boolean isAomenTheMajor (League league) {
		Company company = league.getMajorCompany();
		if (company != null && company == Company.Aomen) {
			return true;
		}
		return false;
	}
}
