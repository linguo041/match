package com.roy.football.match.entities.calculation;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.OFN.response.EuroPl;

import lombok.Data;

@Data
@Entity
@Table(name = "match_company_euro", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class EEuroPlCompany implements Serializable{

	private static final long serialVersionUID = -538324314048341206L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "company", nullable = false)
	@Enumerated(EnumType.STRING)
	private Company company;
	
	@Column(name = "origin_win_pl")
	private Float originEWin;
	@Column(name = "origin_draw_pl")
	private Float originEDraw;
	@Column(name = "origin_lose_pl")
	private Float originELose;
	
	@Column(name = "main_win_pl")
	private Float mainEWin;
	@Column(name = "main_draw_pl")
	private Float mainEDraw;
	@Column(name = "main_lose_pl")
	private Float mainELose;
	
	@Column(name = "current_win_pl")
	private Float currentEWin;
	@Column(name = "current_draw_pl")
	private Float currentEDraw;
	@Column(name = "current_lose_pl")
	private Float currentELose;
	
	@Column(name = "win_change_per_hour")
	private float winChange;
	@Column(name = "draw_change_per_hour")
	private float drawChange;
	@Column(name = "lose_change_per_hour")
	private float loseChange;
	@Column(name = "short_main_win_diff")
	private float smWinDiff;
	@Column(name = "short_main_draw_diff")
	private float smDrawDiff;
	@Column(name = "short_main_lose_diff")
	private float smLoseDiff;
}
