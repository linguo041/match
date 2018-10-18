select m.ofn_match_id, m.league, m.match_time, m.host_name, m.guest_name, mr.host_score, mr.guest_score,
        mp.predict_pk, mpk.current_pk, mp.predict_pk - (mpk.current_pk-(mpk.current_h_win-mpk.current_a_win)/2) pred_curr_diff,
		mpk.home_win_change_rate pk_win_chg,
		mce.current_win_pl, mce.current_draw_pl, mce.current_lose_pl,
		mes.main_avg_win_diff, mes.main_avg_draw_diff, mes.main_avg_lose_diff,
		mce.win_change_per_hour eu_win_chg, mce.draw_change_per_hour eu_draw_chg, mce.lose_change_per_hour eu_lose_chg,
		mls.hot_point, mls.host_att_to_guest latest_h_att, mls.guest_att_to_host latest_g_att,
		mcs.host_level, mcs.guest_level, mcs.host_att_guest_def base_h_att, mcs.guest_att_host_def base_g_att
	from matches m
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id and mpk.company = 'Aomen'
		join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		join match_result mr on m.ofn_match_id = mr.ofn_match_id
		join match_predict mp on m.ofn_match_id = mp.ofn_match_id
		join match_euro_state mes on m.ofn_match_id = mes.ofn_match_id
		join match_club_detail mcd on m.ofn_match_id=mcd.ofn_match_id and mcd.type='All'and mcd.team_id = mcs.host_id
    where 1=1
      and m.cal_phase = 2
	  and mcd.num >= 5
	into outfile '/Users/roy.guo/Documents/match/data_preparing_eu.csv'
		fields terminated by ',' optionally enclosed by '"' lines terminated by '\r\n';

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