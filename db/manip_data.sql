-- Set the Norway Sat-AIS source
update pos set source = 'N';

--------------------------------------------------------------
-- Exact Earth data

-- 1. 
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


-- 2.
-- load positions from pos table into wpos
--drop table wpos;

-- create table wpos (see create_table.sql)

-- insert from LRIT and eE, Norway AIS data
insert into wpos
select distinct * from pos
where 1=1
and date(from_unixtime(ts)) >= '2011-07-01'
and date(from_unixtime(ts)) < '2013-01-01'
and source = 'N'
;
--and lat < 45
--and lat > 30
--and lon < 40
--and lon > -10
;

---------------------------------------------------
-- Tracks table

update tracks
set period = 'WINTER'
where dep = 'GIBRALTAR'
and arr = 'RIO';



