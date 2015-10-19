-- Useful queries

-- Table POS
-- count positions per year
select source, date_format(date(from_unixtime(ts)),'%Y') "Year", count(*)
from pos
where 1=1
group by source, date_format(date(from_unixtime(ts)),'%Y')
order by 2, 1;

-- count positions per month
select source, date_format(date(from_unixtime(ts)),'%Y-%m') "Month", count(*)
from pos
where 1=1
-- and source = 'E'
and date(from_unixtime(ts)) >= '2011-08-01'
and date(from_unixtime(ts)) < '2011-09-01'
group by source, date_format(date(from_unixtime(ts)),'%Y-%m')
order by 2, 1;

-- count position per day
select date_format(date(from_unixtime(ts)),'%Y%m') "Date",
	   count(*) "Position count",
	   truncate(max(lat),0) "Max lat",
	   truncate(min(lat),0) "Min lat", 
	   truncate(max(lon),0)-truncate(min(lon),0) "Coverage lon"
from pos 
where 1=1
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) < '2011-04-01'
--group by date(from_unixtime(ts)) order by 1
;

-----------------------------------------------------------------
-- BEWARE delete!
--
--delete from pos
--where source = 'L'
--;

--drop table pos;


-- Table WPOS
-- count positions between 2 dates
select count(*)
from wpos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-02'
and date(from_unixtime(ts)) < '2011-03-03'
;

-- count positions per month
select source, date_format(date(from_unixtime(ts)),'%Y-%m') "Month", count(*)
from wpos
where 1=1
-- and source = 'E'
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) < '2011-04-01'
group by source, date_format(date(from_unixtime(ts)),'%Y-%m')
order by 2, 1;

-- count position per day
select date_format(date(from_unixtime(ts)),'%Y-%m-%d') "Date",
	   source,
	   count(*) "Position count",
	   truncate(max(lat),0) "Max lat",
	   truncate(min(lat),0) "Min lat", 
	   truncate(max(lon),0)-truncate(min(lon),0) "Coverage lon"
from wpos 
where 1=1
and date(from_unixtime(ts)) >= '2011-03-01'
and date(from_unixtime(ts)) < '2011-04-01'
group by date_format(date(from_unixtime(ts)),'%Y-%m-%d'), source
order by 1, 2
;

-- select timestamps of the last 10 positions in a time interval
select from_unixtime(ts)
from wpos
where 1=1
and from_unixtime(ts) >= '2011-03-02 00:00:00'
and from_unixtime(ts) <  '2011-03-02 00:10:00'
order by ts desc
limit 10
;

-- count top 10 ships with most positions in a time interval
select mmsi, count(*)
from (
	select *
	from wpos
	where 1=1
	and from_unixtime(ts) >= '2011-03-02 00:00:00'
	and from_unixtime(ts) <  '2011-03-02 00:10:00'
) as fpos
where 1=1
group by mmsi
order by 2 desc
limit 10
;

-- count position per day
select date(from_unixtime(ts)),
       count(*), 
	   truncate(max(lat),0) "Max lat",
	   truncate(min(lat),0) "Min lat", 
	   truncate(max(lon),0)-truncate(min(lon),0) "Coverage lon"
from wpos 
where 1=1
and date(from_unixtime(ts)) >= '2013-03-01'
and date(from_unixtime(ts)) < '2013-04-01'
group by date(from_unixtime(ts)) order by 1;

-- select positions within a box
select left(date(from_unixtime(ts)),7), count(*)
from wpos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-02'
and date(from_unixtime(ts)) < '2011-03-03'
and lat between 30 and 40
and lon between -15 and -5
group by left(date(from_unixtime(ts)),7) order by 1;


-- downsample position to 1h rate
select from_unixtime(ts), lat, lon
from (
	select mmsi, max(ts) as ts, lat, lon
	from wpos
	where 1=1
	and from_unixtime(ts) >= '2011-03-02 00:00:00'
	and from_unixtime(ts) <  '2011-03-02 06:00:00'
	group by mmsi, left(from_unixtime(ts), 16)
) as fpos
where 1=1
and mmsi = 740339000
order by 1 asc
;
-- limit 10
INTO OUTFILE '/tmp/pos.csv'
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n';

-- Tracks table
select from_unixtime(ts), from_unixtime(norm_ts), insert_ts
from tracks
where 
mmsi = 256873000
order by norm_ts asc
;

--period = 'WINTER1'
--dep = 'GIBRALTAR'

select dep, arr, period, insert_ts, count(*), count(distinct mmsi) as mmsi
from tracks
group by dep, arr, period
order by insert_ts desc;

select mmsi, dep, arr, period, count(*) as counter
from tracks
where arr = 'GOA'
group by mmsi, dep, arr, period
having counter > 5;

-- dump
C:\Program Files\MySQL\MySQL Server 5.6\bin>mysqldump -uroot -pmysql ai tracks >
 c:\master_data\tracks_20151018_1741.sql


