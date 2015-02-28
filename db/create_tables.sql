//create database ai;

create table pos
(
mmsi varchar(15) not null,
ts int not null,
lat double not null,
lon double not null,
source varchar(1),
index index_mmsi (mmsi),
index index_ts (ts)
);

create table shiptype
(
mmsi varchar(15) not null,
shiptype int not null,
index index_mmsi (mmsi),
index index_shiptype (shiptype)
);

create table wpos
(
mmsi varchar(15) not null,
ts int not null,
lat double not null,
lon double not null,
source varchar(1),
index index_mmsi (mmsi),
index index_ts (ts),
index index_lat (lat),
index index_lon (lon)
);

-- temporary table for importing eE data
create table tpos
(
mmsi varchar(15),
ts varchar(15),
lat varchar(20),
lon varchar(20)
);
