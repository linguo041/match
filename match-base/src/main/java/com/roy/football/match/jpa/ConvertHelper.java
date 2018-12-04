package com.roy.football.match.jpa;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.util.StringUtil;

public class ConvertHelper {
	
	private static Function<String, TeamLabel> TO_TEAM_LABEL = new Function<String, TeamLabel> (){

		@Override
		public TeamLabel apply(String input) {
			return TeamLabel.valueOf(input);
		}
		
	};

	public static List<TeamLabel> strToLabel (String labelStr) {
		if (!StringUtil.isEmpty(labelStr)) {
			String[] labels = labelStr.split(",");
			
			if (labels != null && labels.length > 0) {
				return Lists.transform(Lists.newArrayList(labels), TO_TEAM_LABEL);
			}
		}
		
		return null;
	}
}
