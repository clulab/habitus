from argparse import ArgumentParser
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from sentiment_input_stage import SentimentInputStage
from sentiment_sentiment_stage import SentimentSentimentStage
from typing import Tuple


def get_in_and_out() -> Tuple[str, str]:
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-i", "--input", required=True, help="input file name")
    argument_parser.add_argument("-o", "--output", required=True, help="output file name")
    args = argument_parser.parse_args()
    return args.input, args.output

if __name__ == "__main__":
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    input_file_name: str = "../corpora/ghana-sentiment-tsv/ghana-larger.tsv"
    output_file_name: str = "../corpora/ghana-sentiment-tsv/ghana-larger-sentiment.tsv"
    # input_file_name, output_file_name = get_in_and_out()
    pipeline = Pipeline(
        SentimentInputStage(input_file_name),
        [
            SentimentSentimentStage(sentiment_model_name),
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
