package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.jpa.entities.calculation.EEuroPlCompany.EEuroPlCompanyPk;

import lombok.Data;

@Data
@Entity
@Table(name = "match_pankou")
@IdClass(value = EAsiaPk.EAsiaCompanyPk.class)
public class EAsiaPk implements Serializable{
	
	private static final long serialVersionUID = 4187482226757793530L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Id
	@Column(name = "company")
	@Enumerated(EnumType.STRING)
	private Company company;
	
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
	
	@Column(name = "home_win_change_rate")
	private Float hwinChangeRate;
	@Column(name = "away_win_change_rate")
	private Float awinChangeRate;
	@Column(name = "hours")
	private Float hours;
	
	@Data
	public static class EAsiaCompanyPk implements Serializable{

		private static final long serialVersionUID = 1L;
		private Long ofnMatchId;
		private Company company;
	}
}
