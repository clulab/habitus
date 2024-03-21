from argparse import ArgumentParser
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from vector_input_stage import VectorInputStage
from vector_vector_stage import VectorVectorStage
from typing import Tuple


def get_in_and_out() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-i", "--input", required=True, help="input file name")
    argument_parser.add_argument("-o", "--output", required=True, help="output file name")
    args = argument_parser.parse_args()
    return args.input, args.output

if __name__ == "__main__":
    vector_model_name: str = "all-MiniLM-L6-v2"
    input_file_name: str = "../corpora/ghana-elasticsearch/ghana-elasticsearch-4a.tsv"
    output_file_name: str = "../corpora/ghana-elasticsearch/ghana-elasticsearch-4b.tsv"

    # input_file_name, output_file_name = get_in_and_out()
    pipeline = Pipeline(
        VectorInputStage(input_file_name),
        [
            VectorVectorStage(vector_model_name)
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
