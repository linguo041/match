package com.roy.football.match.jpa.entities.audit;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.roy.football.match.OFN.response.Company;
import com.roy.football.match.base.League;

import lombok.Data;

@Data
@Entity
@Table(name = "league_euro_audit")
public class EEuroAudit implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "audit_id", nullable = false)
    private Long auditId;
	
	@Column(name = "league_id")
	private Long leagueId;
	
	@Column(name = "company")
	@Enumerated(EnumType.STRING)
	private Company company;
	
	@Column(name = "pk")
	private Float pk;
	
	@Column(name = "pk_type")
	@Enumerated(EnumType.STRING)
	private PKType pkType;
	
	@Column(name = "euro_avg_win")
	private Float avgWin;
	@Column(name = "euro_avg_draw")
	private Float avgDraw;
	@Column(name = "euro_avg_lose")
	private Float avgLose;
}
