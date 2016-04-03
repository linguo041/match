package com.roy.football.match.OFN.response;

import java.util.Date;
import java.util.List;

import com.roy.football.match.base.MatchData;

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

	public static class JinCaiMatch implements MatchData, Comparable<JinCaiMatch>{
		public Long getXid() {
			return xid;
		}
		public void setXid(Long xid) {
			this.xid = xid;
		}
		public Long getOddsmid() {
			return oddsmid;
		}
		public void setOddsmid(Long oddsmid) {
			this.oddsmid = oddsmid;
		}
		public Long getLid() {
			return lid;
		}
		public void setLid(Long lid) {
			this.lid = lid;
		}
		public String getLn() {
			return ln;
		}
		public void setLn(String ln) {
			this.ln = ln;
		}
		public Long getSid() {
			return sid;
		}
		public void setSid(Long sid) {
			this.sid = sid;
		}
		public String getHn() {
			return hn;
		}
		public void setHn(String hn) {
			this.hn = hn;
		}
		public String getGn() {
			return gn;
		}
		public void setGn(String gn) {
			this.gn = gn;
		}
		public Long getHtid() {
			return htid;
		}
		public void setHtid(Long htid) {
			this.htid = htid;
		}
		public Long getGtid() {
			return gtid;
		}
		public void setGtid(Long gtid) {
			this.gtid = gtid;
		}
		public Date getMtime() {
			return mtime;
		}
		public void setMtime(Date mtime) {
			this.mtime = mtime;
		}
		public Float getOh() {
			return oh;
		}
		public void setOh(Float oh) {
			this.oh = oh;
		}
		public Float getOd() {
			return od;
		}
		public void setOd(Float od) {
			this.od = od;
		}
		public Float getOa() {
			return oa;
		}
		public void setOa(Float oa) {
			this.oa = oa;
		}

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
