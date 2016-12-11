package com.roy.football.match.util;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author linguo
 *
 */
public class StringUtil {
	public static String UNKNOWN = "UNKNOWN";
	private static NumberFormat cnf = NumberFormat.getCurrencyInstance();
	private static NumberFormat pnf = NumberFormat.getPercentInstance();
	
	static {
		pnf.setMinimumFractionDigits(2);
	}

	public static boolean isEmpty(String v){
        return (null == v || "".equals(v));
    }

    public static boolean NotNull(String v){
        return !isEmpty(v) && !"null".equals(v);
    }

    public static Map<String, String> convertStringToMap (String data) {
		if (StringUtil.isEmpty(data) || data.length() <= 2) {
			return null;
		}

		Map <String, String> map = new HashMap<String, String>();
		String keyValStr = data.substring(1, data.length()-1);
		String [] keyVals = keyValStr.split(",");

		for (String keyVal : keyVals) {
			keyVal = keyVal.trim();
			int eqIndex = keyVal.indexOf('=');

			if (eqIndex != -1) {
				String key = keyVal.substring(0, eqIndex);
				String val = keyVal.substring(eqIndex + 1, keyVal.length());
				map.put(key, val);
			}
		}

		return map;
	}

    public static String formatCurrency (double number) {
    	return cnf.format(number);
    }

    public static String formatCurrency (long number) {
    	return cnf.format(number);
    }

    public static String formatPercent (double number) {
    	return pnf.format(number);
    }
    
    /**-- start ziczhou 2015.06.18 --*/
    /**
     * <p> Convert String s to array String[] with default delimiter which is "," </p>
     * @param string converted string s 
     * @return
     */
    public static List<String> comvertStringToList(String string){
    	StringTokenizer stringTokenizer = new StringTokenizer(string,",");
    	List<String> results = new ArrayList<String>();
    	while(stringTokenizer.hasMoreTokens()){
    		results.add(stringTokenizer.nextToken());
    	}
    	return results;
    }
    
    /**
     * <p>Validate the current string is 
     * @param rawStr
     * @return
     */
	public static boolean isUTF8(String rawStr){
		try {
			byte[] rawText = rawStr.getBytes("UTF-8");
			int score = 0;
			int i, rawTextlen = 0;
			int goodbytes = 0, asciibytes = 0;
			// Maybe also use UTF8 Byte Order Mark: EF BB BF
			// Check to see if characters fit into acceptable ranges
			rawTextlen = rawText.length;
			for (i = 0; i < rawTextlen; i++) {
				if ((rawText[i] & (byte) 0x7F) == rawText[i]) {
					asciibytes++;
					// Ignore ASCII, can throw off count
				} else if (-64 <= rawText[i]
						&& rawText[i] <= -33
						// -0x40~-0x21
						&& // Two bytes
						i + 1 < rawTextlen && -128 <= rawText[i + 1]
						&& rawText[i + 1] <= -65) {
					goodbytes += 2;
					i++;
				} else if (-32 <= rawText[i]
						&& rawText[i] <= -17
						&& // Three bytes
						i + 2 < rawTextlen && -128 <= rawText[i + 1]
						&& rawText[i + 1] <= -65 && -128 <= rawText[i + 2]
						&& rawText[i + 2] <= -65) {
					goodbytes += 3;
					i += 2;
				}
			}
			if (asciibytes == rawTextlen) {
				return false;
			}
			score = 100 * goodbytes / (rawTextlen - asciibytes);
			// If not above 98, reduce to zero to prevent coincidental matches
			// Allows for some (few) bad formed sequences
			if (score > 98) {
				return true;
			} else if (score > 95 && goodbytes > 30) {
				return true;
			} else {
				return false;
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
    /**-- end ziczhou 2015.06.18 --*/
}
