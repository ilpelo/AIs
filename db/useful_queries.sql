-- Useful queries

-- Table POS
-- count positions per month
select left(date(from_unixtime(ts)),7), count(*)
from pos
where source = 'L'
group by left(date(from_unixtime(ts)),7) order by 1;

-- count positions per source/year
select left(date(from_unixtime(ts)),4), source, count(*)
from pos
where source = 'L'
group by left(date(from_unixtime(ts)),4), source
order by 1;

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
delete from pos
where source = 'L';




-- Table WPOS
-- count positions between 2 dates
select count(*)
from wpos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-02'
and date(from_unixtime(ts)) < '2011-03-03'
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
select * from tracks;
delete from tracks;


