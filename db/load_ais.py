from os import listdir

ais_data_dir = 'C:\\master_data\\AIS\\01-Jan\\'
esc_ais_data_dir = ais_data_dir.replace('\\', '\\\\')

pos_dat_files = []
shiptype_dat_files = []
for f in listdir(ais_data_dir):
    if(f.endswith('pos.csv')):
        pos_dat_files += [f]
    if(f.endswith('shiptype.csv')):
        shiptype_dat_files += [f]

for dat in pos_dat_files:
    print("""
load data infile '%s%s' into table pos fields terminated by ',' lines terminated by '\\r\\n';
"""
    % (esc_ais_data_dir, dat))

for dat in shiptype_dat_files:
    print("""
load data infile '%s%s' into table shiptype fields terminated by ',' lines terminated by '\\r\\n';
"""
    % (esc_ais_data_dir, dat))

    
    
    
