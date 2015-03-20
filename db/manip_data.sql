-- insert into pos from temp table for eE AIS data
insert into pos
select mmsi,
       unix_timestamp(str_to_date(ts, '%Y%m%d_%H%i%s')),
	   cast(lat as decimal(24,21)),
	   cast(lon as decimal(24,21)),
	   'E'
from tpos
where mmsi <> 0
  and lat <> ''
  and lon <> ''
  and ts <> '';

-- load positions from pos table into wpos
drop table wpos;

-- create table wpos (see create_table.sql)

-- insert from LRIT and eE, Norway AIS data
insert into wpos
select * from pos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-01'
and date(from_unixtime(ts)) < '2011-03-30';
