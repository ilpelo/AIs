from os import listdir

ais_data_dir = '/data1/sat-ais/'
esc_ais_data_dir = ais_data_dir.replace('\\', '\\\\')
dat_file_prefix = 'emsa_atlantic'


pos_dat_files = []
shiptype_dat_files = []
for f in listdir(ais_data_dir):
    if(f.startswith(dat_file_prefix) and f.endswith('.pos')):
        pos_dat_files += [f]
    if(f.endswith('shiptype.csv')):
        shiptype_dat_files += [f]

output_file = open(ais_data_dir + "load_data.mysql", "w")

for dat in pos_dat_files:
    load_pos = """
load data infile '%s%s' into table tpos fields terminated by ',' enclosed by '"' lines terminated by '\\r\\n' ignore 1 lines (
mmsi, @dummy, @dummy, ts, @dummy,
@dummy, @dummy, @dummy, @dummy, @dummy,
@dummy, @dummy, @dummy, @dummy, @dummy,
@dummy, @dummy, @dummy, @dummy, @dummy,
@dummy, @dummy, @dummy, @dummy, @dummy,
@dummy, @dummy, @dummy, lon, lat,
@dummy, @dummy, @dummy, @dummy, @dummy,
@dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy)
;
""" % (esc_ais_data_dir, dat)
    print(load_pos)
    output_file.write(load_pos)

for dat in shiptype_dat_files:
    load_shiptype = """
load data infile '%s%s' into table shiptype fields terminated by ',' lines terminated by '\\r\\n';
""" % (esc_ais_data_dir, dat)
    print(load_shiptype)
    output_file.write(load_shiptype)

output_file.close()
    
    
