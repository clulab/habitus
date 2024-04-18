
from argparse import ArgumentParser
from sentence_transformers import SentenceTransformer
from tqdm import tqdm
from typing import Tuple

import numpy
import pandas

def get_in_and_out() -> Tuple[str, str]:
	argument_parser = ArgumentParser()
	argument_parser.add_argument("-i", "--input", required=True, help="input file name")
	argument_parser.add_argument("-o", "--output", required=True, help="output file name")
	args = argument_parser.parse_args()
	return args.input, args.output


if __name__ == "__main__":
	sentence_transformer_name: str = "all-MiniLM-L6-v2"
	input_file_name: str = "../corpora/uganda-tsv/uganda/uganda.tsv"
	output_file_name: str = "../corpora/uganda-tsv/uganda/uganda.npy"
	# input_file_name, output_file_name = get_in_and_out()
	data_frame = pandas.read_csv(input_file_name, sep="\t", encoding="utf-8", keep_default_na=False,
		dtype={"file": str, "index": int, "sentence": str, "causal": bool, "belief": bool}
	) # [:100]
	sentence_transformer = SentenceTransformer(sentence_transformer_name)

	data_embeddings = [sentence_transformer.encode(sentence) for sentence in tqdm(data_frame["sentence"])]
	numpy.save(output_file_name, data_embeddings)
