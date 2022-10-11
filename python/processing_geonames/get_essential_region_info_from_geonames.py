import pandas as pd

import argparse
import os
from os import listdir
from os.path import isfile, join



parser = argparse.ArgumentParser(description='Getting essentail info from geonames.org dump files.')
parser.add_argument('input', type=str, help='path to input directory')
parser.add_argument('output', type=str, help='path to output directory')

args = parser.parse_args()
input_dir = args.input
output_dir = args.output

def main():
    files = [f for f in listdir(input_dir) if isfile(join(input_dir, f))]
    for file in files:
        data = pd.read_csv(os.path.join(input_dir, file), header=None, sep="\t")
        df = pd.DataFrame()
        df["region"] = data[2]
        df["country_code"] = data[8]
        out_file = os.path.join(output_dir, file)
        print(out_file)
        df.to_csv(out_file, sep="\t", index=False)

if __name__ == "__main__":
    main()
