package com.roy.football.match.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(name = "euro_pl",
	indexes ={
        @Index(name = "", columnList = "")
    }
)
public class EEuroPl implements Serializable{
	private static final long serialVersionUID = -365910991928120814L;

	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "company_id", nullable = false)
	private Long companyId;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pl_time", nullable = false)
    private Date eTime;
	
	@Column(name = "pl_win", columnDefinition = "decimal(10,3)", nullable = false)
	private Float eWin;
	@Column(name = "pl_draw", columnDefinition = "decimal(10,3)", nullable = false)
	private Float eDraw;
	@Column(name = "pl_lose", columnDefinition = "decimal(10,3)", nullable = false)
	private Float eLose;
}
