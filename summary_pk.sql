
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