package com.roy.football.match.OFN.response;

import java.util.Date;
import java.util.List;

import com.roy.football.match.base.MatchData;

import lombok.Getter;
import lombok.Setter;

public class JinCaiSummary {

	public List<JinCaiMatch> getRows() {
		return rows;
	}

	public void setRows(List<JinCaiMatch> rows) {
		this.rows = rows;
	}
	
	private List<JinCaiMatch> rows;
	
	

	@Override
	public String toString() {
		return "JinCaiSummary [rows=" + rows + "]";
	}

	@Getter
	@Setter
	public static class JinCaiMatch implements MatchData, Comparable<JinCaiMatch>{
	
		private Long xid;  			// match Id
		private Long oddsmid;   	// match Id
		private Long lid;      		// league id
		private String ln;  		// league name
		private Long sid;			// league id something
		private String hn;			// hose name
		private String gn;   		// guest name
		private Long htid;  		// host id
		private Long gtid;			// guest id
		private Date mtime;         // match time
		private Float oh;           // win pay
		private Float od;           // draw pay
		private Float oa;           // lose pay

		@Override
		public int compareTo(JinCaiMatch o) {
			return (int) (this.xid - o.xid);
		}
		@Override
		public String toString() {
			return "JinCaiMatch [xid=" + xid + ", oddsmid=" + oddsmid
					+ ", lid=" + lid + ", ln=" + ln + ", sid=" + sid + ", hn="
					+ hn + ", gn=" + gn + ", htid=" + htid + ", gtid=" + gtid
					+ ", mtime=" + mtime + ", oh=" + oh + ", od=" + od
					+ ", oa=" + oa + "]";
		}
	}
}
