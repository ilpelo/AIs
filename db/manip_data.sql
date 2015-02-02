

-- load positions from pos table into wpos
insert into wpos
select * from pos
where 1=1
and date(from_unixtime(ts)) >= '2011-03-01'
and date(from_unixtime(ts)) < '2011-03-30';