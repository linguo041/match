select * from match_club_detail;

select * from matches m, match_result mr
 where m.ofn_match_id = mr.ofn_match_id
   and m.match_time > '2018-03-28 13:45:00'
 order by m.match_time desc;

select * from matches m, match_result_detail mrd
 where m.ofn_match_id = mrd.ofn_match_id
   and m.match_time > '2018-10-04 13:45:00' 
 order by m.match_time desc;

select * from match_result_detail where 1=1
 -- and host_id = 498
 and guest_id = 498 
 order by ofn_match_id desc;

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
	  and m.match_time > '2018-11-27 13:45:00';


select * from matches where cal_phase < 2;

select * from matches  order by match_time desc

update matches set cal_phase = 0
-- select * from matches
 where cal_phase = 4 and match_time > '2018-7-01 13:45:00'
   and league in ('YingYi'); 
   -- and league not in ('Friendly');

update matches set cal_phase = 4
 where cal_phase = 0 and match_time < '2018-11-19 13:45:00' and league in ('Friendly', 'Euro21Outter', 'Country');
