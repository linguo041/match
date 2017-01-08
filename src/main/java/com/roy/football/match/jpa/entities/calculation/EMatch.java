package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.roy.football.match.OFN.response.EuroPl;
import com.roy.football.match.base.League;

import lombok.Data;

@Data
@Entity
@Table(name = "matches")
public class EMatch implements Serializable{

	private static final long serialVersionUID = -2092993255372976173L;
	
//    @Column(name = "match_id", nullable = false)
//    private Long matchId;

    @Id
    @Column(name = "ofn_match_id", nullable = false)
    private Long ofnMatchId;
    
    @Column(name = "okooo_match_id")
    private Long okoooMatchId;
    
    @Column(name = "match_day_id", nullable = false)
    private Long matchDayId;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "match_time", nullable = false)
    private Date matchTime;
    
    @Column(name = "league", nullable = false)
    @Enumerated(EnumType.STRING)
	private League league;
    
    @Column(name = "host_id", nullable = false)
    private Long hostId;
    
    @Column(name = "host_name", nullable = false)
	private String hostName;
    
    @Column(name = "guest_id", nullable = false)
	private Long guestId;
    
    @Column(name = "guest_name", nullable = false)
	private String guestName;
}
