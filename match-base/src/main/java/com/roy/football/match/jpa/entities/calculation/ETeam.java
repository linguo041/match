package com.roy.football.match.jpa.entities.calculation;

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "teams")
public class ETeam implements Serializable{

	private static final long serialVersionUID = -7257316146065381866L;

	@Id
    @Column(name = "ofn_team_id", nullable = false)
	private Long teamId;
	
	@Column(name = "team_name", nullable = false)
	private String teamName;
	
	@Column(name = "team_name_en")
	private String enTeamName;
	
	@Column(name = "national")
	private Boolean national;
	
	@Column(name = "ft_contry")
	private String contry;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "field")
	private String field;
	
	@Column(name = "city_team_id1")
	private Long cityTeamId1;
	
	@Column(name = "city_team_id2")
	private Long cityTeamId2;
	
	@Column(name = "ft_name")
	private String ftName;
	
	@Column(name = "ft_point")
	private Integer ftPoint;
}
