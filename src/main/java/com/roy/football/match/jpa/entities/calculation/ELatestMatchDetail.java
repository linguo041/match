package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.base.LatestMatchMatrixType;
import com.roy.football.match.base.MatrixType;

import lombok.Data;

@Data
@Entity
@Table(name = "match_latest_detail")
@IdClass(value = ELatestMatchDetail.ELatestMatchDetailPk.class)
public class ELatestMatchDetail implements Serializable{

	private static final long serialVersionUID = 7583667218827454944L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;

	@Id
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private LatestMatchMatrixType type;

	@Column(name = "win_rate")
	private Float winRate;
	@Column(name = "win_draw_rate")
	private Float winDrawRate;
	@Column(name = "win_pk_rate")
	private Float winPkRate;
	@Column(name = "win_draw_pk_rate")
	private Float winDrawPkRate;
	@Column(name = "goal_per_match")
	private Float matchGoal;
	@Column(name = "miss_per_match")
	private Float matchMiss;
	@Column(name = "point")
	private Float point;
	@Column(name = "goal_variation")
	private Float gVariation;
	@Column(name = "miss_variation")
	private Float mVariation;
	
	@Data
	public static class ELatestMatchDetailPk implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long ofnMatchId;
		private LatestMatchMatrixType type;
	}
	
	public static void main (String args[]) {
		ELatestMatchDetail ss = new ELatestMatchDetail();
		ss.setPoint(null);
	}
}
