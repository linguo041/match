package com.roy.football.match.entities.calculation;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "match_jiaoshou", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class EJiaoShou implements Serializable{

	private static final long serialVersionUID = -1408900607270383128L;
	
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;

	@Column(name = "latest_pk")
	private Float latestPankou;
	@Column(name = "latest_dx")
	private Float latestDaxiao;
	@Column(name = "win_rate")
	private Float winRate;
	@Column(name = "win_draw_rate")
	private Float winDrawRate;
	@Column(name = "win_pk_rate")
	private Float winPkRate;
	@Column(name = "win_draw_pk_rate")
	private Float winDrawPkRate;
	@Column(name = "host_goal_per_match")
	private Float hgoalPerMatch;
	@Column(name = "guest_goal_per_match")
	private Float ggoalPerMatch;
	@Column(name = "num")
	private Integer matchNum;
}
