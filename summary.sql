select t.ofn_match_id, t.league, t.match_time, t.host_name, t.guest_name, t.res,
		concat(t.win_cnt, ' - ', t.draw_cnt,  ' - ', t.lose_cnt) win_draw_lose,
        concat(t.win_pk_cnt, ' - ', t.draw_pk_cnt,  ' - ', t.lose_pk_cnt) win_draw_lose_pk,
--         concat(t.win_cnt / (t.win_cnt + t.draw_cnt + t.lose_cnt), ' - ',
-- 			t.draw_cnt / (t.win_cnt + t.draw_cnt + t.lose_cnt), ' - ',
-- 			t.lose_cnt / (t.win_cnt + t.draw_cnt + t.lose_cnt)) win_draw_lose_rate,
-- 		concat(t.win_pk_cnt / (t.win_pk_cnt + t.draw_pk_cnt + t.lose_pk_cnt), ' - ',
-- 			t.draw_pk_cnt / (t.win_pk_cnt + t.draw_pk_cnt + t.lose_pk_cnt), ' - ',
-- 			t.lose_pk_cnt / (t.win_pk_cnt + t.draw_pk_cnt + t.lose_pk_cnt)) win_draw_lose_pk_rate,
        /*t.win_cnt + t.draw_cnt + t.lose_cnt as total,*/
        concat(t.predict_host_score, ' - ', predict_guest_score) predict_score,
        case when t.main_pk != t.current_pk then concat(t.main_pk, ' -> ', t.current_pk) else t.main_pk end as main_current_pk,
		t.predict_pk,
		concat(t.main, ' -> ', t.current) as main_current,
        t.hot_point,
		concat(t.host_level, ' - ', t.guest_level) host_guest_level,
		concat(t.main_h_win, ' -> ', t.current_h_win) main_curr_h_win,
		t.pk_up_change,
        t.aomen_current_pl,
		t.aomen_pl_change,
		t.main_avg_win_diff, t.main_avg_draw_diff, t.main_avg_lose_diff,
		t.host_att_to_guest as latest_h_att_g, t.guest_att_to_host as latest_g_att_h,
		t.host_att_guest_def as h_att_g_def, t.guest_att_host_def as g_att_h_def,
		t.cal_phase
from (
select m1.ofn_match_id, m1.league, m1.match_time, m1.host_name, m1.guest_name, concat(mr1.host_score, ' - ', mr1.guest_score) res,
        sum(case when mr.host_score - mr.guest_score > 0 then 1 else 0 end) win_cnt,
		sum(case when mr.host_score - mr.guest_score = 0 then 1 else 0 end) draw_cnt,
		sum(case when mr.host_score - mr.guest_score < 0 then 1 else 0 end) lose_cnt,
		sum(case when mr.pk_res = 'RangThree' then 1 else 0 end) win_pk_cnt,
		sum(case when mr.pk_res = 'One' then 1 else 0 end) draw_pk_cnt,
		sum(case when mr.pk_res = 'RangZero' then 1 else 0 end) lose_pk_cnt,
        cast(mpk1.main_pk as decimal(5,2)) main_pk, cast(mpk1.current_pk as decimal(5,2)) current_pk,
        mp1.predict_pk, cast(mpk1.main_pk-(mpk1.main_h_win-mpk1.main_a_win)/2 as decimal(5,2)) main,
        cast(mpk1.current_pk-(mpk1.current_h_win-mpk1.current_a_win)/2 as decimal(5,2)) current,
		cast(mpk1.main_h_win as decimal(5,2)) main_h_win, cast(mpk1.current_h_win as decimal(5,2)) current_h_win,
		/*mpk1.current_a_win,*/ cast(mpk1.home_win_change_rate as decimal(5,3)) as pk_up_change,
        concat(mce1.current_win_pl, ' ', mce1.current_draw_pl, ' ', mce1.current_lose_pl) aomen_current_pl,
		concat(mce1.win_change_per_hour, ' ', mce1.draw_change_per_hour, ' ', mce1.lose_change_per_hour) aomen_pl_change,
		mls1.hot_point, mls1.host_att_to_guest, mls1.guest_att_to_host,
		mcs1.host_level, mcs1.guest_level, mcs1.host_att_guest_def, mcs1.guest_att_host_def,
		m1.cal_phase, mes1.main_avg_win_diff, mes1.main_avg_draw_diff, mes1.main_avg_lose_diff,
		cast(mp1.host_score as decimal(5,2)) predict_host_score, cast(mp1.guest_score as decimal(5,2)) predict_guest_score
	from matches m
        left join league l on m.league = l.name
		left join match_club_state mcs on m.ofn_match_id = mcs.ofn_match_id
		left join match_club_detail mcd on m.ofn_match_id=mcd.ofn_match_id and mcd.type='All'and mcd.team_id = mcs.host_id
		left join match_latest_state mls on m.ofn_match_id = mls.ofn_match_id
		left join match_pankou mpk on m.ofn_match_id = mpk.ofn_match_id and mpk.company = 'Aomen'
		left join match_company_euro mce on m.ofn_match_id = mce.ofn_match_id and mce.company = 'Aomen'
		left join match_result mr on m.ofn_match_id = mr.ofn_match_id
		left join match_predict mp on m.ofn_match_id = mp.ofn_match_id
		left join match_euro_state mes on m.ofn_match_id = mes.ofn_match_id
		left join match_jiaoshou mj on m.ofn_match_id = mj.ofn_match_id,
		matches m1
		left join league l1 on m1.league = l1.name
		left join match_club_state mcs1 on m1.ofn_match_id = mcs1.ofn_match_id
		left join match_latest_state mls1 on m1.ofn_match_id = mls1.ofn_match_id
		left join match_pankou mpk1 on m1.ofn_match_id = mpk1.ofn_match_id and mpk1.company = 'Aomen'
		left join match_company_euro mce1 on m1.ofn_match_id = mce1.ofn_match_id and mce1.company = 'Aomen'
		left join match_result mr1 on m1.ofn_match_id = mr1.ofn_match_id
		left join match_predict mp1 on m1.ofn_match_id = mp1.ofn_match_id
		left join match_euro_state mes1 on m1.ofn_match_id = mes1.ofn_match_id
		left join match_jiaoshou mj1 on m1.ofn_match_id = mj1.ofn_match_id
	where m1.ofn_match_id in (select ofn_match_id from matches
			where 1=1
			-- and ofn_match_id in (964419,964958,979148, 986449,964399, 986445, 961399, 969433, 979165, 962035, 959940, 959945, 986464)
            -- and cal_phase = 1
			-- and match_time > '2017-04-10 00:00:00'
			-- and match_time < '2017-04-20 00:00:00'
			and match_time > '2019-04-13 00:00:00'
			-- and match_day_id is not null
			)
      and m.match_time > '2016-10-01 00:00:00'
      and m.cal_phase = 2
	  and abs(l1.goal_per_match - l.goal_per_match) <= 0.3 + abs(l1.goal_per_match - 2.68) * 0.65
      and l.state = 0
      and l.continent = l1.continent
	  -- pk
      and (abs(mpk1.main_pk-0.2) <= 0.75
			and abs((mp.predict_pk - mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) - (mp1.predict_pk - mpk1.main_pk-(mpk1.main_h_win-mpk1.main_a_win)/2)) <= 0.35
		or (abs(mpk1.main_pk-0.2) > 0.75
			and abs((mp.predict_pk - mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) - (mp1.predict_pk - mpk1.main_pk-(mpk1.main_h_win-mpk1.main_a_win)/2)) <= 0.35))
	  and abs((mpk.main_pk-(mpk.main_h_win-mpk.main_a_win)/2) - (mpk1.main_pk-(mpk1.main_h_win-mpk1.main_a_win)/2)) <= 0.08
      and abs((mpk.current_pk-(mpk.current_h_win-mpk.current_a_win)/2) - (mpk1.current_pk-(mpk1.current_h_win-mpk1.current_a_win)/2)) <= 0.075
      and abs(mpk1.current_h_win - mpk.current_h_win) <= 0.07
	  and abs(mpk.home_win_change_rate - mpk1.home_win_change_rate) <= 0.07
	  -- state
	  and abs(mls.hot_point - mls1.hot_point) <= 4
      and abs((mls.host_att_to_guest - mls.guest_att_to_host) - (mls1.host_att_to_guest - mls1.guest_att_to_host)) < 0.4
      -- and abs((mls.host_att_variation_to_guest - mls.guest_att_variation_to_host) - (mls1.host_att_variation_to_guest - mls1.guest_att_variation_to_host)) < 0.75
      -- base
      and (abs(mpk1.main_pk-0.2) <= 0.75 and abs((mcs.host_att_guest_def - mcs.guest_att_host_def) - (mcs1.host_att_guest_def - mcs1.guest_att_host_def)) < 0.5
		or abs(mpk1.main_pk-0.2) > 0.75 and abs((mcs.host_att_guest_def - mcs.guest_att_host_def) - (mcs1.host_att_guest_def - mcs1.guest_att_host_def)) < 0.6)
      and abs(mcs.host_level - mcs.guest_level - mcs1.host_level + mcs1.guest_level) <= 2
      and mcd.num >= 5
	  -- js
      -- and (abs(mj.win_draw_pk_rate - mj1.win_draw_pk_rate) <= 0.4 or abs(mj.win_draw_rate - mj1.win_draw_rate) <= 0.4)
	  -- pl
	  and (mce1.main_win_pl < 2.601 and abs(mce1.main_win_pl - mce.main_win_pl) <= mce1.main_win_pl * 0.065
		or mce1.main_lose_pl < 2.601 and abs(mce1.main_lose_pl - mce.main_lose_pl) <= mce1.main_lose_pl * 0.065)
	  and (mce1.current_win_pl < 2.601 and abs(mce1.current_win_pl - mce.current_win_pl) <= mce1.current_win_pl * 0.045
		or mce1.current_lose_pl < 2.601 and abs(mce1.current_lose_pl - mce.current_lose_pl) <= mce1.current_lose_pl * 0.045)
	  -- and ((abs(mce1.current_win_pl - mce.current_win_pl) <= 0.08 or abs(mce1.current_lose_pl - mce.current_lose_pl) <= 0.08) and abs(mce1.current_draw_pl - mce.current_draw_pl) <= 0.2)
	  and ( mpk1.main_pk > 0 and (mce1.win_change_per_hour >= 0 and mce.win_change_per_hour > -0.011 or mce1.win_change_per_hour <= 0 and mce.win_change_per_hour < 0.011)
        or mpk1.main_pk <= 0 and (mce1.lose_change_per_hour >=0 and mce.lose_change_per_hour > -0.011 or mce1.lose_change_per_hour <=0 and mce.lose_change_per_hour < 0.011))
	  group by m1.ofn_match_id, l1.goal_per_match
      order by m1.match_time asc
) t 
 where 1=1 order by t.match_time asc, t.ofn_match_id