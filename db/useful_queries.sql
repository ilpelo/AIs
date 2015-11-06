-- Useful queries

-- Table POS
-- count positions per year
select source, date_format(date(from_unixtime(ts)),'%Y') "Year", count(*)
from pos
where 1=1
group by source, date_format(date(from_unixtime(ts)),'%Y')
order by 2, 1;

-- count positions per month
select date_format(date(from_unixtime(ts)),'%Y-%m') "Month", count(*)
from pos
where 1=1
-- and source = 'E'
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) <= '2012-12-31'
group by date_format(date(from_unixtime(ts)),'%Y-%m')
order by 1;

-- count positions per month/source
select source, date_format(date(from_unixtime(ts)),'%Y-%m') "Month", count(*)
from pos
where 1=1
-- and source = 'E'
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) <= '2012-12-31'
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

-- count distinct ships by source
select source, count(distinct mmsi)
from pos 
where 1=1
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) < '2011-02-01'
group by source;

-- count distinct ships 
select count(distinct mmsi)
from pos 
where 1=1
and (source = 'E' or source = 'N')
and date(from_unixtime(ts)) >= '2011-01-01'
and date(from_unixtime(ts)) < '2011-02-01'
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
select mmsi, lon, lat, dep, arr, period, from_unixtime(ts), from_unixtime(norm_ts), insert_ts
from tracks
where 
dep = "REDSEA"
and mmsi = 240800000
order by norm_ts asc
;

--mmsi = 256873000
--period = 'WINTER1'
--dep = 'GIBRALTAR'

select dep, arr, period, insert_ts, from_unixtime(insert_ts), count(*), count(distinct mmsi) as mmsi
from tracks
where 1=1
and dep = 'CHANNEL'
and arr = 'NOVASCOTIA'
and period = 'WINTER'
group by dep, arr, period, insert_ts
order by insert_ts desc;

select mmsi, dep, arr, period, 
count(*) as counter, 
min(ts) as min_ep_ts, 
max(ts) as max_ep_ts,
min(from_unixtime(ts)) as min_ts, 
max(from_unixtime(ts)) as max_ts,
min(from_unixtime(norm_ts)) as min_norm_ts, 
max(from_unixtime(norm_ts)) as max_norm_ts
from tracks
where 1=1
and dep = 'CHANNEL' -- 'GIBRALTAR' --
and arr = 'NOVASCOTIA' -- 'GUADELOUPE' --
and period = 'SUMMER' -- 'SPRING' --'SUMMER'
group by mmsi, dep, arr, period
having counter > 5;

-- dump
C:\Program Files\MySQL\MySQL Server 5.6\bin>mysqldump -uroot -pmysql ai tracks >
 c:\master_data\tracks_20151018_1741.sql
 
 -- Mine voyages
 java -cp minevoyages.jar org.pelizzari.mine.MineVoyages C:\master_data\conf\lanzarote-natal-spring-2011.props


