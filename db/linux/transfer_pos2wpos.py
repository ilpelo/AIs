import sys
year = int(sys.argv[1])
for x in range(1,13):
    y = x+1
    year1 = year
    if(x == 12):
        year1 = year + 1
        y = 1
    print("select now() as '';")
    print("select 'transferring %s-%02d' as '';" % (year, x))
    print("""
insert into wpos select distinct * from pos where 1=1 and date(from_unixtime(ts)) >= '%d-%02d-01' and date(from_unixtime(ts)) < '%d-%02d-01';"""
          % (year, x, year1, y))

