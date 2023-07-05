from argparse import ArgumentParser
from belief_stage import BeliefStage
from location_stage import LocationStage
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from sentiment_stage import SentimentStage
from similarity_stage import SimilarityStage
from text_input_stage import TextInputStage
from typing import Tuple
from web_input_stage import WebInputStage


def get_in_and_out() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-i", "--input", required=True, help="input directory name")
    argument_parser.add_argument("-o", "--output", required=True, help="output file name")
    args = argument_parser.parse_args()
    return args.input, args.output

if __name__ == "__main__":
    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    locations_file_name: str = "./belief_pipeline/GH.tsv"    
    # input_dir_name: str = "../corpora/Galamsey-web-435"
    # output_file_name: str = "../sample_output.tsv"
    input_dir_name, output_file_name = get_in_and_out()
    pipeline = Pipeline(
        # WebInputStage(input_dir_name),
        TextInputStage(input_dir_name),
        [
            BeliefStage(belief_model_name),
            SimilarityStage(),
            SentimentStage(sentiment_model_name),
            LocationStage(locations_file_name)
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
