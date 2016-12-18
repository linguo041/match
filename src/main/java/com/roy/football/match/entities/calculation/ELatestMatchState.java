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

import com.roy.football.match.base.TeamLevel;

import lombok.Data;

@Data
@Entity
@Table(name = "match_latest_state", indexes =
    {
        @Index(name = "", columnList = "")
    }
)
public class ELatestMatchState implements Serializable{

	private static final long serialVersionUID = -1101530069702589939L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "league_id", nullable = false)
	private Long leagueId;
	
	@Column(name = "host_id", nullable = false)
    private Long hostId;
	
	@Column(name = "guest_id", nullable = false)
    private Long guestId;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ofn_match_id")
	private List<ELatestMatchDetail> latestDetails;
	
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
