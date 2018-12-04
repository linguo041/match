package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.roy.football.match.base.League;
import com.roy.football.match.base.TeamLevel;

import lombok.Data;

@Data
@Entity
@Table(name = "match_latest_state")
public class ELatestMatchState implements Serializable{

	private static final long serialVersionUID = -1101530069702589939L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "league", nullable = false)
    @Enumerated(EnumType.STRING)
	private League league;
	
//	@Column(name = "host_id", nullable = false)
//    private Long hostId;
//	
//	@Column(name = "guest_id", nullable = false)
//    private Long guestId;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ofn_match_id")
//	@Transient
	private Set<ELatestMatchDetail> latestDetails;
	
	@Column(name = "host_att_to_guest")
	private Float hostAttackToGuest;
	@Column(name = "guest_att_to_host")
	private Float guestAttackToHost;
	@Column(name = "host_att_variation_to_guest")
	private Float hostAttackVariationToGuest;
	@Column(name = "guest_att_variation_to_host")
	private Float guestAttackVariationToHost;
	
	@Column(name = "pk_by_latest_matches")
	private Float pkByLatestMatches;
	
	@Column(name = "hot_point")
	private Float hotPoint;
}
