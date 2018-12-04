with sample as (
 select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name,
		mr.host_score, mr.guest_score, mpk.main_pk, mpk.main_h_win, mpk.current_h_win, mpk.current_a_win, mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
), single as(
 select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name,
		mr.host_score, mr.guest_score, mpk.main_pk, mpk.main_h_win, mpk.current_h_win, mpk.current_a_win, mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
    where m.ofn_match_id = 961386
)
select sample.*
  from sample, single
 where sample.main_pk = single.main_pk
   and abs(sample.current_h_win - single.current_h_win) < 0.05
   and abs(sample.home_win_change_rate - single.home_win_change_rate) < 0.05
   and abs(sample.hot_point - single.hot_point) <= 3;



-- 输半博全 （适用于主队让球； 客队让球，客队状态不能太好，主队要好）
select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, mp.predict_pk - (mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) main,
        mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
	  and mpk.current_pk = 0.25
      and mp.predict_pk - (mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) between -0.45 and 0.2
	--  上盘高水
	  and mpk.current_h_win >= 1.02
	  and mpk.home_win_change_rate > 0
	--  主队状态不差
	  and mls.hot_point >= -3
      and mls.hot_point <= 4
      -- and (mcs.host_att_guest_def - mcs.guest_att_host_def) > 0
     order by m.match_time desc;

select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, mpk.main_pk, mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
      and mpk.main_pk <= -0.25
	  and mpk.current_pk = -0.25
      and mp.predict_pk - (mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) between -0.35 and 0.25
	--  上盘高水
	  and mpk.current_a_win >= 1.02
	  and mpk.away_win_change_rate < 0
	--  客队状态不差
	  and mls.hot_point >= -4
      and mls.hot_point <= 3
      -- and (mcs.host_att_guest_def - mcs.guest_att_host_def) > 0
     order by m.match_time desc;

-- 半球生死 （1.8，1.82 不靠谱）
select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, mpk.main_pk, mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
        mce.main_win_pl, mce.main_draw_pl, mce.main_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
	  -- and mpk.main_pk = 0.5
	  and mpk.current_pk = 0.5
      and mr.host_score <= mr.guest_score
      -- and mp.predict_pk - mpk.current_pk >= -0.55
      and mp.predict_pk - mpk.current_pk > -0.15
      and mp.predict_pk - mpk.current_pk < 0.4
	--  
	  and mpk.current_h_win <= 1.02
	  -- and mpk.home_win_change_rate < 0
	--  
	  and mls.hot_point > -3.5
	  -- and mls.hot_point <= 7
      -- and (mcs.host_att_guest_def - mcs.guest_att_host_def) > 0
     order by m.match_time desc;


select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, cast(mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 as decimal(5,3)) main,
        mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
	  and mpk.current_pk = 0.5
      and mpk.current_pk = mpk.main_pk and mpk.current_h_win = mpk.main_h_win
      and cal_phase = 2
     order by m.match_time desc;

select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, cast(mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 as decimal(5,3)) main,
        mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
      and mp.predict_pk < (mpk.current_pk-(mpk.current_h_win-mpk.current_a_win)/2)
      and mpk.current_pk between -1.01 and 1.01
      and mpk.main_h_win < 0.86
      and (mpk.current_pk-(mpk.current_h_win-mpk.current_a_win)/2) - (mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) > (mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) * 0.15
      and mls.hot_point > -2
	  and cal_phase = 2
     order by m.match_time desc;

select p.league,  p.matches, p.goal/p.matches goal_per_match, p.net_goal/p.matches net_goal_per_match,
	p.shot/p.matches shot_per_match, p.shot_on_target/p.matches sot_per_match, p.fault/p.matches fault_per_match
 from (
	select t.league, count(t.ofn_match_id) matches, sum(t.goal) goal, sum(t.net_goal) net_goal, sum(t.shot) shot, sum(t.shot_on_target) shot_on_target, sum(t.fault) fault
	 from (
		select m.ofn_match_id, m.league,
				(mrd.host_score +mrd.guest_score) goal,
                abs(mrd.host_score-mrd.guest_score) net_goal,
				(mrd.host_shot +mrd.guest_shot) shot,
				(mrd.host_shot_on_target +mrd.guest_shot_on_target) shot_on_target,
				(mrd.host_fault +mrd.guest_fault) fault
			  from matches m, match_result mr, match_result_detail mrd 
			 where 1=1
			   and m.ofn_match_id = mr.ofn_match_id
			   and m.ofn_match_id = mrd.ofn_match_id) as t
	 where 1=1
		and t.goal is not null
		and t.shot is not null
		and t.shot_on_target is not null
		and t.fault is not null
	 group by t.league
) as p where p.matches > 20;

/* Check how many matches don't have euro data*/
select count(p.ofn_match_id) from (
select m.* from matches m left join match_company_euro mce
 on m.ofn_match_id = mce.ofn_match_id
where 1=1
  and m.cal_phase =2 
  and league != 'Friendly'
  and m.match_time > '2016-01-01 00:00:00'
  group by m.ofn_match_id
  having count(mce.ofn_match_id) < 1) p;



update league,
(select p.league,  p.matches, p.goal/p.matches goal_per_match, p.net_goal/p.matches net_goal_per_match
  from(
    select t.league, count(t.ofn_match_id) matches, sum(t.goal) goal, sum(t.net_goal) net_goal
	 from (
		select m.ofn_match_id, m.league,
				(mrd.host_score +mrd.guest_score) goal,
                abs(mrd.host_score-mrd.guest_score) net_goal
		  from matches m, match_result_detail mrd 
	     where 1=1
		   and m.ofn_match_id = mrd.ofn_match_id
		  ) as t
	where 1=1
	  and t.goal is not null
	group by t.league
	) p where p.matches > 20) tmp 
set league.goal_per_match = tmp.goal_per_match, league.net_goal_per_match=tmp.net_goal_per_match
where league.name = tmp.league;

select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, concat(mr.host_score, ' - ', mr.guest_score) res,
        mp.predict_pk, cast(mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 as decimal(5,3)) main,
        mpk.current_pk, mpk.main_h_win, mpk.current_h_win, /*mpk.current_a_win,*/ mpk.home_win_change_rate,
		mpk1.current_pk, mpk1.main_h_win, mpk1.current_h_win, /*mpk.current_a_win,*/ mpk1.home_win_change_rate,
        mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl, mce.win_change_per_hour, mce.draw_change_per_hour, mce.lose_change_per_hour,
		mls.hot_point, mls.host_att_to_guest, mls.guest_att_to_host,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def, mcs.guest_att_host_def,
		m.cal_phase
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id and mpk.company = 'Aomen'
		left join match_pankou mpk1 on m.ofn_match_id = mpk1.ofn_match_id and mpk1.company = 'YiShenBo'
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
    where 1=1
      and mpk.current_pk = 0.75 and mpk.current_h_win < 0.8
      and mpk.home_win_change_rate < -0.05
      -- and mpk.main_pk = 0.75 and mpk.main_h_win < 0.92
      -- and mpk.current_h_win <= mpk. main_h_win
	  and mpk1.current_pk = 1 and mpk1.current_h_win > 1
      -- and abs(mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 - mp.predict_pk) < 0.3
	  and cal_phase = 2
     order by m.match_time desc;


select m.ofn_match_id, m.match_day_id, m.match_time, m.league, m.host_name, m.guest_name,
        concat(mr.host_score, ' - ', mr.guest_score) res,
		mce.win_change_per_hour as w_change,
		mce.draw_change_per_hour as d_change,
		mce.lose_change_per_hour as l_change,	
		mls.hot_point,
		mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 as main, mp.predict_pk,
        mpk.home_win_change_rate
	from matches m
        left join league l on m.league = l.name
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_jiaoshou js on m.ofn_match_id=js.ofn_match_id
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
        left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
        left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id and mpk.company = 'Aomen'
        left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
	where 1=1 and m.cal_phase =2
        and mce.win_change_per_hour < -0.015 /* pl down*/
        -- and mce.draw_change_per_hour > 0.015
        and mls.hot_point > 3  /*hot*/
        and mpk.main_pk > 0.2
		-- and mpk.home_win_change_rate < 0.001
        and mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2 - mp.predict_pk > 0.10
		-- and m.ofn_match_id = 959978;
		and mr.host_score = mr.guest_score
		and m.match_time > '2016-01-15 0:00:00';

delete from league_euro_audit;

insert into league_euro_audit (league_id, name, company, pk, pk_type, euro_avg_win, euro_avg_draw, euro_avg_lose)
select t.league_id, t.name, t.company, t.current_pk, 'Current', t.avg_win, t.avg_draw, t.avg_lose from (
	select l.league_id, l.name, mpk.company, mpk.current_pk,
		   sum(mce.current_win_pl)/count(mce.current_win_pl) avg_win,
		   sum(mce.current_draw_pl)/count(mce.current_draw_pl) avg_draw,
		   sum(mce.current_lose_pl)/count(mce.current_lose_pl) avg_lose,
           count(m.ofn_match_id) cnt
	  from matches m, match_company_euro mce, match_pankou mpk, league l
	 where mce.company='Aomen'
	   and mpk.company='Aomen'
	   and mpk.ofn_match_id = mce.ofn_match_id
	   and m.ofn_match_id = mce.ofn_match_id
       and m.league = l.name
	  group by l.league_id, l.name, mpk.current_pk) t
 where t.cnt > 20;

