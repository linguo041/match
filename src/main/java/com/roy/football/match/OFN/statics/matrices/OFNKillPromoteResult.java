package com.roy.football.match.OFN.statics.matrices;

import java.util.Set;
import java.util.TreeSet;

import com.roy.football.match.OFN.MatchPromoter.MatchPull;
import com.roy.football.match.OFN.MatchPromoter.MatchRank;
import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.process.KillResult;
import com.roy.football.match.process.PromoteResult;

import lombok.Data;

@Data
public class OFNKillPromoteResult implements KillResult, PromoteResult{

	private Set<ResultGroup> killByPk = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> killByPl = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> killByPlPkUnmatch = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> killByExchange = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> killByPull = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> promoteByBase = new TreeSet<ResultGroup> ();
	private Set<ResultGroup> promoteByPull = new TreeSet<ResultGroup> ();
	private MatchRank rank = new MatchRank();
	private MatchPull pull = new MatchPull();
}
