package com.roy.football.match.entities.calculation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.roy.football.match.OFN.response.Company;

import lombok.Data;

@Data
@Entity
@Table(name = "match_euro_state", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class EEuroPlState implements Serializable{

	private static final long serialVersionUID = -3951938628441760114L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ofn_match_id")
	private List<EEuroPlCompany> companyPls;
	
	@Column(name = "main_avg_win_diff")
	private float mainAvgWinDiff;
	@Column(name = "main_avg_draw_diff")
	private float mainAvgDrawDiff;
	@Column(name = "main_avg_lose_diff")
	private float mainAvgLoseDiff;
}
