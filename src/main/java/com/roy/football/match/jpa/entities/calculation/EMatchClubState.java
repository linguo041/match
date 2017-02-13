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
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;

import lombok.Data;

@Data
@Entity
@Table(name = "match_club_state")
public class EMatchClubState implements Serializable{

	private static final long serialVersionUID = 6253082811418037813L;

	@Id
	@Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
	
	@Column(name = "league")
	private League league;
	
	@Column(name = "host_id", nullable = false)
    private Long hostId;
	
	@Column(name = "guest_id", nullable = false)
    private Long guestId;
	
	@OneToMany(cascade={CascadeType.ALL})
	@JoinColumn(name="ofn_match_id")
//	@Transient
	private Set<EMatchClubDetail> clubStateDetails;
	
	@Column(name = "host_level")
	@Enumerated(EnumType.ORDINAL)
	private TeamLevel hostLevel;
	
	@Column(name = "guest_level")
	@Enumerated(EnumType.ORDINAL)
	private TeamLevel guestLevel;
	
	@Column(name = "host_label")
	private String hostLabel;
	
	@Column(name = "guest_label")
	private String guestLabel;
	
	@Column(name = "host_att_guest_def")
	private Float hostAttGuestDefInx;
	
	@Column(name = "guest_att_host_def")
	private Float guestAttHostDefInx;
}
