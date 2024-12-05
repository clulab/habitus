from argparse import ArgumentParser
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from tpi_belief_stage import TpiBeliefStage
from tpi_input_stage import TpiInputStage
from tpi_location_stage import TpiLocationStage
from tpi_resolution_stage import TpiResolutionStage
from tpi_sentiment_stage import TpiSentimentStage
from typing import Tuple

import sys

def get_locations() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-l", "--location", required=True, help="location file name")
    args = argument_parser.parse_args()
    return args.location

if __name__ == "__main__":
    sys.stdin.reconfigure(encoding="utf-8", newline='\n') # just in case!
    sys.stdout.reconfigure(encoding="utf-8", newline='\n') # just in case!

    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    locations_file_name: str = get_locations()
    pipeline = Pipeline(
        TpiInputStage(None),
        [
            TpiResolutionStage(),
            TpiBeliefStage(belief_model_name),
            TpiSentimentStage(sentiment_model_name),
            TpiLocationStage(locations_file_name)
        ],
        PandasOutputStage(None)
    )

    while (True):
        pipeline.run()
