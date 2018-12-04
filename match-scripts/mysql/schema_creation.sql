use mysql;

create database if not exists football default character set utf8 collate utf8_bin;

create user 'gambler'@'localhost' identified by 'bet@match888';

insert into db (host,db,user,select_priv,insert_priv,update_priv,delete_priv,create_priv,drop_priv,index_priv, alter_priv) values('localhost','football','gambler','y','y','y','y','n','n','n','n');

flush privileges;