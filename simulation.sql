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