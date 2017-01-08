package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "match_exchange")
public class EExchange implements Serializable{

	private static final long serialVersionUID = 4720010635518886966L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "bf_win_exchange")
	private Long bfWinExchange;
	@Column(name = "bf_draw_exchange")
	private Long bfDrawExchange;
	@Column(name = "bf_lose_exchange")
	private Long bfLoseExchange;
	@Column(name = "bf_win_rate")
	private Float bfWinExgRt;
	@Column(name = "bf_draw_rate")
	private Float bfDrawExgRt;
	@Column(name = "bf_lose_rate")
	private Float bfLoseExgRt;
	@Column(name = "bf_win_gain")
	private Integer bfWinGain;
	@Column(name = "bf_draw_gain")
	private Integer bfDrawGain;
	@Column(name = "bf_lose_gain")
	private Integer bfLoseGain;
	
	@Column(name = "jc_win_exchange")
	private Long jcWinExchange;
	@Column(name = "jc_draw_exchange")
	private Long jcDrawExchange;
	@Column(name = "jc_lose_exchange")
	private Long jcLoseExchange;
	@Column(name = "jc_win_rate")
	private Float jcWinExgRt;
	@Column(name = "jc_draw_rate")
	private Float jcDrawExgRt;
	@Column(name = "jc_lose_rate")
	private Float jcLoseExgRt;
	@Column(name = "jc_win_gain")
	private Integer jcWinGain;
	@Column(name = "jc_draw_gain")
	private Integer jcDrawGain;
	@Column(name = "jc_lose_gain")
	private Integer jcLoseGain;
}
