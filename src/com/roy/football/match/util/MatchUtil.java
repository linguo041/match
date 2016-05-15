package com.roy.football.match.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.roy.football.match.OFN.response.AsiaPl;
import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices;
import com.roy.football.match.OFN.statics.matrices.EuroMatrices.EuroMatrix;
import com.roy.football.match.base.League;
import com.roy.football.match.base.TeamLabel;

public class MatchUtil {
	public final static String UNICODE_WIN = "\u8d62";
	public final static String UNICODE_LOSE = "\u8f93";
	public final static String UNICODE_GREAT = "\u5927";
	public final static String UNICODE_LESS = "\u5c0f";
	public final static String UNICODE_DRAW = "\u8d70";
	public final static Character UP_ARROW = '↑';
	public final static Character DOWN_ARROW = '↓';
	public final static long DAY_TIME = 86400000;
	public final static long YEAR_TIME = 86400 * 900; // unit is second
	
	public final static String simple_date_format = "yyMMdd";
	
	public static EuroMatrix getMainEuro (EuroMatrices euroMatrices, League league) {
		Company company = league.getMajorCompany();
		if (company != null) {
			switch (company) {
				case Aomen:
					return euroMatrices.getAomenMatrix();
				case SNAI:
					return euroMatrices.getSnaiMatrix();
				default:
					return euroMatrices.getWilliamMatrix();
			}
		}

		return euroMatrices.getWilliamMatrix();
	}
	
	public static boolean isHostHomeStrong (List <TeamLabel> hostLabels) {
		if (hostLabels != null && hostLabels.size() > 0) {
			for (TeamLabel l : hostLabels) {
				if (TeamLabel.HomeStrong == l) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean isGuestDefensive (List <TeamLabel> guestLabels) {
		if (guestLabels != null && guestLabels.size() > 0) {
			for (TeamLabel l : guestLabels) {
				if (TeamLabel.Defensive == l) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static float getEuDiff (float eu1, float eu2, boolean abs) {
		float temp;
		
		if (abs) {
			temp = Math.abs(eu1 - eu2);
		} else {
			temp = eu1 - eu2;
		}
		
		return temp/eu2;
	}
	
	public static float getCalculatedPk (AsiaPl asiaPk) {
		float pankou = asiaPk.getPanKou();
		float winP = asiaPk.gethWin();
		float loseP = asiaPk.getaWin();
		
		return pankou - (winP - loseP)/2;
	}

	public static String getMatchDay () {
		DateFormat df = new SimpleDateFormat(simple_date_format);
		
		return df.format(new Date());
	}
	
	public static Date parseFromOFHString (String dtStr) {
		if (dtStr == null || dtStr.isEmpty()) {
			return null;
		}
		return new Date(Long.parseLong(dtStr)*1000);
	}
	
	public static boolean isMatchInTwoYear (Date matchDate, Date currentDate) {
		if ((currentDate.getTime() - matchDate.getTime())/1000 <= YEAR_TIME) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isMatchTooOld (Date matchDate, Date currentDate, int indicator) {
		return isMatchTooOld(matchDate, currentDate, indicator, 17);
	}
	
	public static boolean isMatchTooOld (Date matchDate, Date currentDate, int indicator, int init) {
		if (currentDate.getTime() - matchDate.getTime() > (long)24 * 3600 * 1000 * (7 * indicator + init)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static float getDiffHours (Date current, Date another) {
		long cTime = current.getTime();
		long aTime = another.getTime();
		
		return (float)(cTime - aTime) / 3600000;
	}
	
	public static Float parsePankouString (String pkString) {
		if (pkString == null || pkString.isEmpty()) {
			return null;
		}
		
		int sIndex = pkString.indexOf("/");
		if (sIndex > 0) {
			String token1 = pkString.substring(0, sIndex);
			String token2 = pkString.substring(sIndex + 1);
			
			return (Float.parseFloat(token1) + Float.parseFloat(token2))/2;
		}
		
		return Float.parseFloat(pkString);
	}
	
	public static void main (String [] args) {
		float f1 = parsePankouString("3");
		float f2 = parsePankouString("0/-0.5");
		float f3 = parsePankouString("1/1.5");
		
		System.out.println(f1 + "    " + f2 + "    " + f3);
		
		System.out.println(String.format("%.2f", 0.3453535f));
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		System.out.println((day + month * 100 + (year - 2000) * 10000) * 1000);
	}
}
