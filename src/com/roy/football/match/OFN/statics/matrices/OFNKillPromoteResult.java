package com.roy.football.match.OFN.statics.matrices;

import java.util.Set;

import com.roy.football.match.base.ResultGroup;
import com.roy.football.match.process.KillResult;
import com.roy.football.match.process.PromoteResult;

public class OFNKillPromoteResult implements KillResult, PromoteResult{

	public Set<ResultGroup> getKillByPk() {
		return killByPk;
	}
	public void setKillByPk(Set<ResultGroup> killByPk) {
		this.killByPk = killByPk;
	}
	public Set<ResultGroup> getKillByPl() {
		return killByPl;
	}
	public void setKillByPl(Set<ResultGroup> killByPl) {
		this.killByPl = killByPl;
	}
	public Set<ResultGroup> getPromoteByPk() {
		return promoteByPk;
	}
	public void setPromoteByPk(Set<ResultGroup> promoteByPk) {
		this.promoteByPk = promoteByPk;
	}
	public Set<ResultGroup> getTooHot() {
		return tooHot;
	}
	public void setTooHot(Set<ResultGroup> tooHot) {
		this.tooHot = tooHot;
	}

	private Set<ResultGroup> killByPk;
	private Set<ResultGroup> killByPl;
	private Set<ResultGroup> promoteByPk;
	private Set<ResultGroup> tooHot;
}
