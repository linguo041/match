package com.roy.football.match.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.roy.football.match.OFN.parser.OFNTeamFetcher;
import com.roy.football.match.OFN.parser.OFNTeamFetcher.OfnTeamName;
import com.roy.football.match.OFN.parser.TeamRankingFetcher.TeamRanking;
import com.roy.football.match.OFN.parser.TeamRankingFetcher;
import com.roy.football.match.OFN.statics.matrices.CalculatedAndResult;
import com.roy.football.match.jpa.entities.calculation.ETeam;
import com.roy.football.match.jpa.repositories.TeamRepository;
import com.roy.football.match.jpa.service.MatchPersistService;
import com.roy.football.match.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TeamService {
	@Autowired
	private TeamRepository teamRepository;
	
	@Autowired
	private OFNTeamFetcher ofnTeamFetcher;
	
	@Autowired
	private TeamRankingFetcher teamRankingFetcher;

	@Autowired
	public ExecutorService calculateExecutorService;
	
	public void fetchTeamName () {
		List<ETeam> teams = teamRepository.findTeamsWithoutName();
		
		List<Future<Long>> futures = Lists.newArrayListWithCapacity(teams.size());
		
		for (ETeam et : teams) {
			futures.add(calculateExecutorService.submit(() -> {
				OfnTeamName tn = ofnTeamFetcher.fetch(et.getTeamId());
				
				et.setTeamName(tn.getName());
				et.setEnTeamName(tn.getEnName());
				et.setCity(tn.getCity());
				et.setField(tn.getField());
				
				teamRepository.save(et);
				return et.getTeamId();
			}));
		}
		
		for (Future<Long> f : futures) {
			try {
				Long teamId = f.get();
				log.info("finished fetch team {}", teamId);
			} catch (InterruptedException | ExecutionException e) {
				log.error("Unable to fetch team data");
			}
		}
	}
	
	public void fetchTeamRanking () {
		List<TeamRanking> teams = Lists.newArrayList();
		for (int ii = 1; ii <= 15; ii++ ) {
			List<TeamRanking> fetched = teamRankingFetcher.fetch(ii);
			teams.addAll(fetched);
			log.info("fetched ft teams {}",fetched);
		}

		List<ETeam> dbTeams = (List<ETeam>) teamRepository.findAll();
		
		Map<String, TeamRanking> rankTeams = Maps.newHashMap();
		for (TeamRanking tr : teams) {
			rankTeams.put(tr.getEnName(), tr);
		}
		
		for (ETeam et : dbTeams) {
			String ftEnName = et.getFtName();
			TeamRanking ftTeam = rankTeams.get(ftEnName);
			if (!StringUtil.isEmpty(ftEnName) && ftTeam != null) {
				if (ftTeam.getPoint() != null && et.getFtPoint() != ftTeam.getPoint()) {
					et.setFtPoint(ftTeam.getPoint());
					log.info("update team point {}", et);
					teamRepository.save(et);
				}
			} else {
				String eCnName = et.getTeamName();
				String eEnName = et.getEnTeamName();
				boolean found = false;
				
				for (TeamRanking tr : teams) {
					if ( tr.getEnName() != null && tr.getEnName().equals(eEnName)
							|| tr.getCnName() != null && tr.getCnName().equals(eCnName)) {
						found = true;
						et.setFtName(tr.getEnName());
						et.setFtPoint(tr.getPoint());
						et.setContry(tr.getContry());
						log.info("found team by team_name_en, save team ranking {}", et);
						teamRepository.save(et);
						break;
					}
				}
				
				if (!found) {
					log.info("team not found {}", et);
				}
			}
		}
	}
}
