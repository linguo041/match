package com.roy.football.match.entities.calculation;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "match_daxiao_pk", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class EDaXiaoPk implements Serializable{

	private static final long serialVersionUID = -683234055386222084L;

	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "origin_h_win")
	private Float originHWin;
	@Column(name = "origin_a_win")
	private Float originAWin;
	@Column(name = "origin_pk")
	private Float originPk;
	@Column(name = "main_h_win")
	private Float mainHWin;
	@Column(name = "main_a_win")
	private Float mainAWin;
	@Column(name = "main_pk")
	private Float mainPk;
	@Column(name = "current_h_win")
	private Float currentHWin;
	@Column(name = "current_a_win")
	private Float currentAWin;
	@Column(name = "current_pk")
	private Float currentPk;
	
	@Column(name = "da_change_rate")
	private Float daChangeRate;
	@Column(name = "xiao_change_rate")
	private Float xiaoChangeRate;
	@Column(name = "hours")
	private Float hours;
}
