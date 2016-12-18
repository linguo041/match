package com.roy.football.match.entities;

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
@Table(name = "daxiao_pk",
	indexes ={
        @Index(name = "", columnList = "")
    }
)
public class EDaXiaoPk {
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "company_id", nullable = false)
	private Long companyId;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pk_time", nullable = false)
    private Date eTime;
	
	@Column(name = "pk_host", columnDefinition = "decimal(10,3)", nullable = false)
	private Float hWin;
	@Column(name = "pk_guest", columnDefinition = "decimal(10,3)", nullable = false)
	private Float aWin;
	@Column(name = "pk", columnDefinition = "decimal(10,3)", nullable = false)
	private Float panKou;
}
