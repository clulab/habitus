from belief_stage import BeliefStage
from location_stage import LocationStage
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from sentiment_stage import SentimentStage
from similarity_stage import SimilarityStage
from text_input_stage import TextInputStage
from web_input_stage import WebInputStage

if __name__ == "__main__":
    # This is called from the top-level directory.
    input_dir_name: str = "./corpora/Galamsey-web-435/"
    output_file_name: str = "./sample_output.tsv"
    belief_model_name: str = "./models/bert-base-cased-best-f1-0.6717557251908397"
    locations_file_name: str = "./corpora/ghana_geonames/GH.tsv"
    sentiment_model_name: str = "./models/finetuned_beliefs_sentiment_classifier_experiment1"
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
