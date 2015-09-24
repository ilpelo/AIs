from os import listdir

ais_data_dir = '/data1/sat-ais/2011/01-Jan'
esc_ais_data_dir = ais_data_dir.replace('\\', '\\\\')

pos_dat_files = []
shiptype_dat_files = []
for f in listdir(ais_data_dir):
    if(f.endswith('pos.csv')):
        pos_dat_files += [f]
    if(f.endswith('shiptype.csv')):
        shiptype_dat_files += [f]

output_file = open(ais_data_dir + "load_data.mysql", "w")

for dat in pos_dat_files:
    load_pos = """
load data infile '%s/%s' into table pos fields terminated by ',' lines terminated by '\\n';
""" % (esc_ais_data_dir, dat)
    print(load_pos)
    output_file.write(load_pos)

for dat in shiptype_dat_files:
    load_shiptype = """
load data infile '%s/%s' into table shiptype fields terminated by ',' lines terminated by '\\n';
""" % (esc_ais_data_dir, dat)
    print(load_shiptype)
    output_file.write(load_shiptype)

output_file.close()
    
    
