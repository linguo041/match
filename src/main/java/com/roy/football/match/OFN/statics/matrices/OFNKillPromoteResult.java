package com.roy.football.match.OFN.statics.matrices;

import java.util.Set;

import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.process.KillResult;
import com.roy.football.match.process.PromoteResult;

import lombok.Data;

@Data
public class OFNKillPromoteResult implements KillResult, PromoteResult{

	private Set<ResultGroup> killByPk;
	private Set<ResultGroup> killByPl;
	private Set<ResultGroup> killByPlPkUnmatch;
	private Set<ResultGroup> killByExchange;
	private Set<ResultGroup> promoteByPk;
}
