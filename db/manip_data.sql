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

delete from tracks
where arr = 'GOA'
and dep = 'REDSEA'
and insert_ts = 1444752436;

update tracks set ts = norm_ts
where arr = 'GOA'
and dep = 'REDSEA';

delete 
--select count(*) 
from tracks
where 1=1
and dep = 'LANZAROTE'
and arr = 'NATAL'
and period = 'AUTUMN'
and insert_ts <> 1444927311;


-- delete ships with few positions
delete from tracks
where arr = 'REUNION'
and dep = 'CAPETOWN'
and mmsi in
(
	select mmsi from
	(
		select mmsi, dep, arr, period, count(*) as counter
		from tracks
		where arr = 'REUNION'
		and dep = 'CAPETOWN'
		group by mmsi, dep, arr, period
		having counter < 10
	) few_pos
) 
;

