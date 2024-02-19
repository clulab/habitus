from argparse import ArgumentParser
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from tpi_belief_stage import TpiBeliefStage
from tpi_input_stage import TpiInputStage
from tpi_location_stage import TpiLocationStage
from tpi_resolution_stage import TpiResolutionStage
from tpi_sentiment_stage import TpiSentimentStage
from typing import Tuple


def get_in_and_out() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-i", "--input", required=True, help="input file name")
    argument_parser.add_argument("-o", "--output", required=True, help="output file name")
    args = argument_parser.parse_args()
    return args.input, args.output

if __name__ == "__main__":
    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    locations_file_name: str = "./belief_pipeline/UG.tsv"    
    input_file_name: str = "../corpora/uganda-local/uganda.tsv"
    output_file_name: str = "../corpora/uganda-local/uganda-2.tsv"
    # input_file_name, output_file_name = get_in_and_out()
    pipeline = Pipeline(
        TpiInputStage(input_file_name),
        [
            TpiResolutionStage(),
            TpiBeliefStage(belief_model_name),
            TpiSentimentStage(sentiment_model_name),
            TpiLocationStage(locations_file_name)
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
