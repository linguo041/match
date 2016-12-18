package com.roy.football.match.entities.calculation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.base.LatestMatchMatrixType;
import com.roy.football.match.base.MatrixType;

import lombok.Data;

@Data
@Entity
@Table(name = "match_latest_detail", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class ELatestMatchDetail {
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
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
}
