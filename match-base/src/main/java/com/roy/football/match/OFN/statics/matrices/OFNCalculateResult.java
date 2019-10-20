package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.OFN.response.MatchResultAnalyzed;
import com.roy.football.match.base.League;
import com.roy.football.match.base.MatchData;
import com.roy.football.match.okooo.MatchExchangeData;
import com.roy.football.match.process.CalculateResult;

import lombok.Data;

@Data
public class OFNCalculateResult implements CalculateResult, MatchData {

	private League league;
	private boolean distinctHomeAway = false;
	private ClubMatrices clubMatrices;
	private JiaoShouMatrices jiaoShou;
	private MatchState matchState;
	private PankouMatrices pkMatrices;
	private PankouMatrices ysbPkMatrices;
	private DaxiaoMatrices dxMatrices;
	private Float predictPanKou;
	private PredictResult predictResult;
	private EuroMatrices euroMatrices;
	private MatchExchangeData exchanges;
	private MatchResultAnalyzed hostMra;
	private MatchResultAnalyzed guestMra;
}
