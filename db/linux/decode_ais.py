from os import listdir
import sys

# the java command must be run in the data directory

#ais_data_dir = '/data1/sat-ais/2011/01-Jan/'
ais_data_dir = sys.argv[1]

dat_files = []
for f in listdir(ais_data_dir):
    if(f.endswith('.dat')):
        dat_files += [f]

for dat in dat_files:
    print("""
java -cp /data1/lib/aisdecode.jar org.pelizzari.AisDecode \"%s\" \"%s_pos.csv\" \"%s_shiptype.csv\"
"""
    % (dat, dat, dat))
