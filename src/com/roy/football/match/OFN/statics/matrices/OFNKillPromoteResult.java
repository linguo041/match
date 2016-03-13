package com.roy.football.match.OFN.statics.matrices;

import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.process.KillResult;
import com.roy.football.match.process.PromoteResult;

public class OFNKillPromoteResult implements KillResult, PromoteResult{

	public ResultGroup getKillByPk() {
		return killByPk;
	}

	public void setKillByPk(ResultGroup killByPk) {
		this.killByPk = killByPk;
	}

	public ResultGroup getPromoteByPk() {
		return promoteByPk;
	}

	public void setPromoteByPk(ResultGroup promoteByPk) {
		this.promoteByPk = promoteByPk;
	}

	public ResultGroup getTooHot() {
		return tooHot;
	}

	public void setTooHot(ResultGroup tooHot) {
		this.tooHot = tooHot;
	}

	public ResultGroup getKillByPl() {
		return killByPl;
	}

	public void setKillByPl(ResultGroup killByPl) {
		this.killByPl = killByPl;
	}

	private ResultGroup killByPk;
	private ResultGroup killByPl;
	private ResultGroup promoteByPk;
	private ResultGroup tooHot;
}
