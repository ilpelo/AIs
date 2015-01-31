drop table pos;

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