-- Useful queries

select left(date(from_unixtime(ts)),7), count(*) from pos group by left(date(from_unixtime(ts)),7) order by 1;