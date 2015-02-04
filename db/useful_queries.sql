-- Useful queries

-- Table POS
-- count positions per month
select left(date(from_unixtime(ts)),7), count(*)
from pos
group by left(date(from_unixtime(ts)),7) order by 1;

-- count position per day
select date(from_unixtime(ts)) "Date",
	   count(*) "Position count",
	   truncate(max(lat),0) "Max lat",
	   truncate(min(lat),0) "Min lat", 
	   truncate(max(lon),0)-truncate(min(lon),0) "Coverage lon"
from pos 
group by date(from_unixtime(ts)) order by 1;



-- Table WPOS
-- count positions between 2 dates
select count(*)
from wpos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-02'
and date(from_unixtime(ts)) < '2011-03-03'
;

-- count position per day
select date(from_unixtime(ts)), count(*) 
from wpos 
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
