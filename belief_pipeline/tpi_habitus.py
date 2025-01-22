# import os
# import sys

# print("Keith was here.", file=sys.stderr)
# print("The current directory is ", os.getcwd(), file=sys.stderr)


from argparse import ArgumentParser
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from tpi_belief_stage import TpiBeliefStage
from tpi_input_stage import TpiInputStage
from tpi_location_stage_with_patch import TpiLocationStage
from tpi_resolution_stage import TpiResolutionStage
from tpi_sentiment_stage import TpiSentimentStage
from vector_vector_stage import VectorVectorStage
from typing import Tuple

import os
import sys

def get_arguments() -> Tuple[str, str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-l", "--location", required=True,  help="location file name")
    argument_parser.add_argument("-i", "--input",    required=False, help="input file name")
    argument_parser.add_argument("-o", "--output",   required=False, help="output file name")
    args = argument_parser.parse_args()
    return (args.location, args.input, args.output)

def log(message: str):
    with open("log.txt", "a", encoding="utf-8", newline="\n") as file:
        print(message, file=file)

if __name__ == "__main__":
    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    vector_model_name: str = "all-MiniLM-L6-v2"
    locations_file_name, input_file_name, output_file_name = get_arguments()
    if not input_file_name:
        sys.stdin.reconfigure(encoding="utf-8", newline='\n') # just in case!
    if not output_file_name:
        sys.stdout.reconfigure(encoding="utf-8", newline='\n') # just in case!
    # TODO: add a way to quiet the output, i.e., to suppress progress bars
    pipeline = Pipeline(
        TpiInputStage(input_file_name),
        [
            TpiResolutionStage(),
            TpiBeliefStage(belief_model_name),
            TpiSentimentStage(sentiment_model_name),
            TpiLocationStage(locations_file_name),
            VectorVectorStage(vector_model_name)
        ],
        PandasOutputStage(output_file_name)
    )

    log("The program started.")
    while (True):
        pipeline.run()
