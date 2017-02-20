package com.roy.football.match.jpa.entities.calculation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.roy.football.match.OFN.response.Company;

import lombok.Data;

@Data
@Entity
@Table(name = "league")
public class ELeague {
	@Id
	@Column(name = "league_id", nullable = false)
    private Long leagueId;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "main_company")
	@Enumerated(EnumType.STRING)
	private Company mainCompany;
	
	@Column(name = "team_num")
	private Integer teamNum;
	
	@Column(name = "state")
	private Boolean state;
	
	@Column(name = "goal_per_match")
	private Float goalPerMatch;
	
	@Column(name = "net_goal_per_match")
	private Float netGoalPerMatch; 

}
