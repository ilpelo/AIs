for x in range(1,13):
    print("""
load data infile 'C:\\\\master_data\\\\LRIT\\\\2011-%02d.csv'
into table pos fields terminated by ',' lines terminated by '\\r\\n'
ignore 1 lines
(@dummy,     @dummy,     @dummy,             @dummy,     @dummy,     @dummy,     @dummy,     lat,     lon,      @ts,        @dummy,            @dummy,           @dummy,      mmsi,         @dummy)
set source = 'L', ts = truncate(unix_timestamp(@ts),0);"""
          % (x))
