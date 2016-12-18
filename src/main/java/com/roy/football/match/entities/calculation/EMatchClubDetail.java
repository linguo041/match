package com.roy.football.match.entities.calculation;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.base.MatrixType;

import lombok.Data;

@Data
@Entity
@Table(name = "match_club_detail", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class EMatchClubDetail implements Serializable{

	private static final long serialVersionUID = -3033658090621553898L;
	
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private MatrixType type;
	
	@Column(name = "num")
	private Integer num;            // total match number
	@Column(name = "win_rate")
	private Float winRt;            // win / num
	@Column(name = "win_draw_rate")
	private Float winDrawRt;        // (win + draw) / num
	@Column(name = "draw_lose_rate")
	private Float drawLoseRt;       // (draw + lose) / num
	@Column(name = "goals")
	private Integer goals;          // total goaled goals
	@Column(name = "misses")
	private Integer misses;         // total missed goals
	@Column(name = "win_goals")
	private Integer winGoals;       // win goals = goal - miss
	@Column(name = "game_win_lose_diff")
	private Integer winLoseDiff;    // |win - lose|
	@Column(name = "pm")
	private Integer pm;             // pai ming
	@Column(name = "score")
	private Integer point;          // scores(win-3, draw-1, lose-0)
}
