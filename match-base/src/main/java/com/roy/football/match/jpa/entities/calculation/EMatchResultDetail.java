package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.roy.football.match.base.League;

import lombok.Data;

@Data
@Entity
@Table(name = "match_result_detail")
public class EMatchResultDetail implements Serializable{

	private static final long serialVersionUID = -8353332910744354768L;
	
	@Id
	@Column(name = "ofn_match_id", nullable = false)
	private Long ofnMatchId;
	
	@Column(name = "league")
    @Enumerated(EnumType.STRING)
	private League league;
	
	@Column(name = "host_id", nullable = false)
	private Long hostId;
	
	@Column(name = "host_name")
	private String hostName;
	
	@Column(name = "guest_id", nullable = false)
	private Long guestId;
	
	@Column(name = "guest_name")
	private String guestName;

	@Column(name = "host_score")
	private Integer hostScore;
	@Column(name = "guest_score")
	private Integer guestScore;
	@Column(name = "host_shot")
	private Integer hostShot;
	@Column(name = "guest_shot")
	private Integer guestShot;
	@Column(name = "host_shot_on_target")
	private Integer hostShotOnTarget;
	@Column(name = "guest_shot_on_target")
	private Integer guestShotOnTarget;
	@Column(name = "host_fault")
	private Integer hostFault;
	@Column(name = "guest_fault")
	private Integer guestFault;
	@Column(name = "host_corner")
	private Integer hostCorner;
	@Column(name = "guest_corner")
	private Integer guestCorner;
	@Column(name = "host_offside")
	private Integer hostOffside;
	@Column(name = "guest_offside")
	private Integer guestOffside;
	@Column(name = "host_yellowcard")
	private Integer hostYellowCard;
	@Column(name = "guest_yellowcard")
	private Integer guestYellowCard;
	@Column(name = "host_time")
	private Float hostTime;
	@Column(name = "guest_time")
	private Float guestTime;
	@Column(name = "host_save")
	private Integer hostSave;
	@Column(name = "guest_save")
	private Integer guestSave;
}
