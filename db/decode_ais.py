from os import listdir

# the java command must be run in the data directory

ais_data_dir = 'C:\\master_data\\AIS\\03-Mar'

dat_files = []
for f in listdir(ais_data_dir):
    if(f.endswith('.dat')):
        dat_files += [f]

for dat in dat_files:
    print("""
java -cp C:\\master_data\\lib\\aisdecode.jar org.pelizzari.AisDecode \"%s\" \"%s_pos.csv\" \"%s_shiptype.csv\"
"""
    % (dat, dat, dat))


    
    
    
