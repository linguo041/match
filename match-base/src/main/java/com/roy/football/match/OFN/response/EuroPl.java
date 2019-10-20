package com.roy.football.match.OFN.response;

import java.util.Date;

import com.roy.football.match.base.MatchData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EuroPl implements MatchData {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eDraw == null) ? 0 : eDraw.hashCode());
		result = prime * result + ((eLose == null) ? 0 : eLose.hashCode());
		result = prime * result + ((eWin == null) ? 0 : eWin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EuroPl other = (EuroPl) obj;
		if (eDraw == null) {
			if (other.eDraw != null)
				return false;
		} else if (!eDraw.equals(other.eDraw))
			return false;
		if (eLose == null) {
			if (other.eLose != null)
				return false;
		} else if (!eLose.equals(other.eLose))
			return false;
		if (eWin == null) {
			if (other.eWin != null)
				return false;
		} else if (!eWin.equals(other.eWin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EuroPl [eWin=" + eWin + ", eDraw=" + eDraw + ", eLose=" + eLose
				+ ", eDate=" + eDate + "]";
	}

	private Float eWin;
	private Float eDraw;
	private Float eLose;
	private Date eDate;
}
