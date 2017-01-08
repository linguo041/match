package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.roy.football.match.base.ResultGroup;

import lombok.Data;

@Data
@Entity
@Table(name = "match_result")
public class EMatchResult implements Serializable {

	private static final long serialVersionUID = -5273111356421553461L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "host_score")
	private Integer hostScore;
	
	@Column(name = "guest_score")
	private Integer guestScore;
	
	@Column(name = "pk_res")
	private ResultGroup pkResult;
	
	@Column(name = "daxiao_res")
	private ResultGroup dxResult;
	
	@Column(name = "pl_res")
	private ResultGroup plResult;
	
//	@OneToOne(cascade={CascadeType.ALL})
//	@JoinColumn(name="ofn_match_id")
	@Transient
	private EMatchResultDetail eMatchResultDetail;
}
