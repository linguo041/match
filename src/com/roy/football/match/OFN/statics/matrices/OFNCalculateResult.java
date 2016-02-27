package com.roy.football.match.OFN.statics.matrices;

import java.util.List;

import com.roy.football.match.base.MatchData;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.process.CalculateResult;

public class OFNCalculateResult implements CalculateResult, MatchData {

	public ClubMatrices getClubMatrices() {
		return clubMatrices;
	}
	public void setClubMatrices(ClubMatrices clubMatrices) {
		this.clubMatrices = clubMatrices;
	}
	public TeamLevel getGuestLevel() {
		return guestLevel;
	}
	public void setGuestLevel(TeamLevel guestLevel) {
		this.guestLevel = guestLevel;
	}
	public TeamLevel getHostLevel() {
		return hostLevel;
	}
	public void setHostLevel(TeamLevel hostLevel) {
		this.hostLevel = hostLevel;
	}
	public List<TeamLabel> getHostLabels() {
		return hostLabels;
	}
	public void setHostLabels(List<TeamLabel> hostLabels) {
		this.hostLabels = hostLabels;
	}
	public List<TeamLabel> getGuestLabels() {
		return guestLabels;
	}
	public void setGuestLabels(List<TeamLabel> guestLabels) {
		this.guestLabels = guestLabels;
	}

	private ClubMatrices clubMatrices;
	private TeamLevel hostLevel;
	private List<TeamLabel> hostLabels;
	private TeamLevel guestLevel;
	private List<TeamLabel> guestLabels;
}
