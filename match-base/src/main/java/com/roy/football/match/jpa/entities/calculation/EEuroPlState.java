package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.roy.football.match.OFN.response.Company;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of="ofnMatchId")
@Entity
@Table(name = "match_euro_state")
public class EEuroPlState implements Serializable{

	private static final long serialVersionUID = -3951938628441760114L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ofn_match_id")
//	@Transient
	private Set<EEuroPlCompany> companyPls;
	
	@Column(name = "avg_win")
	private Float avgWin;
	@Column(name = "avg_draw")
	private Float avgDraw;
	@Column(name = "avg_lose")
	private Float avgLose;
	
	@Column(name = "main_avg_win_diff")
	private float mainAvgWinDiff;
	@Column(name = "main_avg_draw_diff")
	private float mainAvgDrawDiff;
	@Column(name = "main_avg_lose_diff")
	private float mainAvgLoseDiff;
}
