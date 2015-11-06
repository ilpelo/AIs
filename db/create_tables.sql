//create database ai;

-- temporary table for importing eE data
create table tpos
(
mmsi varchar(15),
ts varchar(15), -- sec (not millisec!)
lat varchar(20),
lon varchar(20)
);

-- position archive
create table pos
(
mmsi varchar(15) not null,
ts int not null, -- sec (not millisec!)
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

-- positions, working table
-- keeps only some months to speed up analysis process
create table wpos
(
mmsi varchar(15) not null,
ts int not null, -- sec (not millisec!)
lat double not null,
lon double not null,
source varchar(1),
index index_mmsi (mmsi),
index index_ts (ts),
index index_lat (lat),
index index_lon (lon)
);

-- tracks table
-- keeps positions of tracks between 2 regions
create table tracks
(
mmsi varchar(15) not null,
ts int not null, -- sec (not millisec!)
lat double not null,
lon double not null,
source varchar(1),
period varchar(30) not null,
dep varchar(30) not null,
arr varchar(30) not null,
index index_mmsi (mmsi),
index index_ts (ts),
index index_dep_arr (dep, arr)
);

alter table tracks add column norm_ts int;

-- fitness record
create table fitness
(
period varchar(30) not null,
dep varchar(30) not null,
arr varchar(30) not null,
gen int not null,
distance_err double not null,
dest_err double not null,
head_err double not null,
var_err double not null,
cov_err double not null,
distance_factor double not null,
dest_factor double not null,
head_factor double not null,
var_factor double not null,
cov_factor  double not null,
fitness double not null,
insert_ts int not null,
index index_ts (insert_ts),
index index_dep_arr (dep, arr, gen, insert_ts)
);



