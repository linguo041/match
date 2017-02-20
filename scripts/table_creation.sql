create table matches (
  ofn_match_id bigint not null,
  okooo_match_id bigint,
  match_day_id bigint,
  match_time datetime,
  league varchar(20),
  host_id int not null,
  host_name varchar(40),
  guest_id int not null,
  guest_name varchar(40),
  cal_phase int,
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_pankou (
  ofn_match_id bigint not null,
  company varchar(40) not null,
  origin_h_win decimal(10,3),
  origin_a_win decimal(10,3),
  origin_pk decimal(10,3),
  main_h_win decimal(10,3),
  main_a_win decimal(10,3),
  main_pk decimal(10,3),
  current_h_win decimal(10,3),
  current_a_win decimal(10,3),
  current_pk decimal(10,3),
  home_win_change_rate decimal(10,4),
  away_win_change_rate decimal(10,4),
  hours int,
  primary key (ofn_match_id, company)
) ENGINE=InnoDB;

create table match_daxiao_pk (
  ofn_match_id bigint not null,
  origin_h_win decimal(10,3),
  origin_a_win decimal(10,3),
  origin_pk decimal(10,3),
  main_h_win decimal(10,3),
  main_a_win decimal(10,3),
  main_pk decimal(10,3),
  current_h_win decimal(10,3),
  current_a_win decimal(10,3),
  current_pk decimal(10,3),
  da_change_rate decimal(10,4),
  xiao_change_rate decimal(10,4),
  hours int,
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_euro_state (
  ofn_match_id bigint not null,
  avg_win decimal(10,4),
  avg_draw decimal(10,4),
  avg_lose decimal(10,4),
  main_avg_win_diff decimal(10,4),
  main_avg_draw_diff decimal(10,4),
  main_avg_lose_diff decimal(10,4),
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_company_euro (
  ofn_match_id bigint not null,
  company varchar(40) not null,
  origin_win_pl decimal(10,4),
  origin_draw_pl decimal(10,4),
  origin_lose_pl decimal(10,4),
  main_win_pl decimal(10,4),
  main_draw_pl decimal(10,4),
  main_lose_pl decimal(10,4),
  current_win_pl decimal(10,4),
  current_draw_pl decimal(10,4),
  current_lose_pl decimal(10,4),
  win_change_per_hour decimal(10,4),
  draw_change_per_hour decimal(10,4),
  lose_change_per_hour decimal(10,4),
  short_main_win_diff decimal(10,4),
  short_main_draw_diff decimal(10,4),
  short_main_lose_diff decimal(10,4),
  primary key (ofn_match_id, company)
) ENGINE=InnoDB;

create table match_exchange (
  ofn_match_id bigint not null,
  bf_win_exchange int,
  bf_draw_exchange int,
  bf_lose_exchange int,
  bf_win_rate decimal(10,4),
  bf_draw_rate decimal(10,4),
  bf_lose_rate decimal(10,4),
  bf_win_gain int,
  bf_draw_gain int,
  bf_lose_gain int,
  jc_win_exchange int,
  jc_draw_exchange int,
  jc_lose_exchange int,
  jc_win_rate decimal(10,4),
  jc_draw_rate decimal(10,4),
  jc_lose_rate decimal(10,4),
  jc_win_gain int,
  jc_draw_gain int,
  jc_lose_gain int,
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_jiaoshou (
  ofn_match_id bigint not null,
  latest_pk decimal(10,4),
  latest_dx decimal(10,4),
  win_rate decimal(10,4),
  win_draw_rate decimal(10,4),
  win_pk_rate decimal(10,4),
  win_draw_pk_rate decimal(10,4),
  host_goal_per_match decimal(10,4),
  guest_goal_per_match decimal(10,4),
  num int,
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_latest_state (
  ofn_match_id bigint not null,
  league varchar(20),
  host_att_to_guest decimal(10,4),
  guest_att_to_host decimal(10,4),
  host_att_variation_to_guest decimal(10,4),
  guest_att_variation_to_host decimal(10,4),
  pk_by_latest_matches decimal(10,4),
  hot_point decimal(10,2),
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_latest_detail (
  ofn_match_id bigint not null,
  type varchar(20) not null,
  win_rate decimal(10,4),
  win_draw_rate decimal(10,4),
  win_pk_rate decimal(10,4),
  win_draw_pk_rate decimal(10,4),
  goal_per_match decimal(10,2),
  miss_per_match decimal(10,2),
  point decimal(10,2),
  goal_variation decimal(10,4),
  miss_variation decimal(10,4),
  primary key (ofn_match_id, type)
) ENGINE=InnoDB;

create table match_club_state (
  ofn_match_id bigint not null,
  league varchar(20),
  host_id int,
  guest_id int,
  host_level int,
  guest_level int,
  host_label varchar(100),
  guest_label varchar(100),
  host_att_guest_def decimal(10,4),
  guest_att_host_def decimal(10,4),
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_club_detail (
  ofn_match_id bigint not null,
  team_id  bigint not null,
  type varchar(20) not null,
  num int,
  goals int,
  misses int,
  win_goals int,
  game_win_lose_diff int,
  pm int,
  score int,
  win_rate decimal(10,4),
  win_draw_rate decimal(10,4),
  draw_lose_rate decimal(10,4),
  primary key (ofn_match_id, team_id, type)
) ENGINE=InnoDB;

create table match_result (
  ofn_match_id bigint not null,
  host_score int,
  guest_score int,
  pk_res varchar(20), 
  daxiao_res varchar(20),
  pl_res varchar(20),
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_result_detail (
  ofn_match_id bigint not null,
  league varchar(20),
  host_id int not null,
  host_name varchar(40),
  guest_id int not null,
  guest_name varchar(40),
  host_score int,
  guest_score int,
  host_shot int,
  guest_shot int,
  host_shot_on_target int,
  guest_shot_on_target int,
  host_fault int,
  guest_fault int,
  host_corner int,
  guest_corner int,
  host_offside int,
  guest_offside int,
  host_yellowcard int,
  guest_yellowcard int,
  host_time decimal(10,4),
  guest_time decimal(10,4),
  host_save int,
  guest_save int,
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table match_predict (
  ofn_match_id bigint not null,
  last_match_pk decimal(10,3),
  predict_pk decimal(10,3),
  host_score decimal(5,2),
  guest_score decimal(5,2),
  primary key (ofn_match_id)
) ENGINE=InnoDB;

create table league (
	league_id bigint not null,
	name varchar(20),
	main_company varchar(20),
	team_num int,
	state int,
	goal_per_match decimal(10,4),
	net_goal_per_match decimal(10,4)，
	primary key (league_id)
) ENGINE=InnoDB;

create index league_name ON football.league (name);
