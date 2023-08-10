from argparse import ArgumentParser
from causal_sentence_belief_stage import CausalSentenceBeliefStage
from causal_sentence_resolution_stage import CausalSentenceResolutionStage
from causal_sentence_input_stage import CausalSentenceInputStage
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from typing import Tuple


def get_in_and_out() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-i", "--input", required=True, help="input file name")
    argument_parser.add_argument("-o", "--output", required=True, help="output file name")
    args = argument_parser.parse_args()
    return args.input, args.output

if __name__ == "__main__":
    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    # input_file_name: str = "../corpora/causalSentences.tsv"
    # output_file_name: str = "../corpora/causalBeliefSentences.tsv"
    input_file_name, output_file_name = get_in_and_out()
    pipeline = Pipeline(
        CausalSentenceInputStage(input_file_name),
        [
            CausalSentenceResolutionStage(),
            CausalSentenceBeliefStage(belief_model_name),
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
