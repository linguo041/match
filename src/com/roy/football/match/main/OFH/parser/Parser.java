package com.roy.football.match.main.OFH.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	private static Pattern KEY_VALUE_REG = Pattern.compile("\\b(\\w+)\\b\\s*=\\s*(.+?);");
	
	public void parse (String rawData) {
		Matcher matcher = KEY_VALUE_REG.matcher(rawData);

		while (matcher.find()) {
			String key = matcher.group(1);
			String var = matcher.group(2);
			System.out.println(key + "    " + var);
		}
	}
	
	public static void main (String [] args) {
		String data3 = "var a = ddd; var mm=4; var nn = {abc:1}; var b=}; var c = 4";
		new Parser().parse(data3);
	}
}
