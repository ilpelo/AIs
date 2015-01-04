create table position
(
mmsi varchar(15) not null,
ts int not null,
lat double not null,
lon double not null,
index index_mmsi (mmsi),
index index_ts (ts)
);