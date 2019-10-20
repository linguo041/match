package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.roy.football.match.base.ResultGroup;

import lombok.Data;

@Data
@Entity
@Table(name = "match_predict")
public class EPredictResult implements Serializable {

	private static final long serialVersionUID = -7953430922860482004L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "last_match_pk")
    private Float lastMatchPk;
	
//	@Column(name = "this_match_pk")
//    private Float thisMatchPk;
//	
//	@Column(name = "this_match_pk_ratio")
//    private Float thisMatchPkRatio;
	
	@Column(name = "predict_pk")
    private Float predictPk;
	
//	@Column(name = "hot_ratio")
//    private Integer hotRatio;
//	
//	@Column(name = "cross_match")
//    private Long crossMatch;
	
	@Column(name = "host_score")
	private Float hostScore;
	
	@Column(name = "guest_score")
	private Float guestScore;
	
//	@Column(name = "kill_by_pk")
//	private String killByPk;
//	@Column(name = "kill_by_pl")
//	private String killByPl;
//	@Column(name = "kill_by_exg")
//	private String killByExchange;
//	@Column(name = "promote_by_pk")
//	private String promoteByPk;
//	@Column(name = "promote_by_pl")
//	private String promoteByPl;
//	@Column(name = "promote")
//	private String promote;
}
