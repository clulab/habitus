from pandas import DataFrame
from pandas_output_stage import PandasOutputStage
from pipeline import OutputStage
from pipeline import Pipeline
from sentiment_stage import SentimentStage

import pandas

class SentimentInputStage():
    def __init__(self, file_name: str) -> None:
        self.file_name = file_name

    def run(self) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", encoding="utf-8", header=0, names=["belief", "title", "author", "year", "context", "just_belief"])
        return data_frame

class SentimentOutputStage(OutputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(file_name)

    def run(self, data_frame: DataFrame) -> None:
        data_frame.to_csv(self.file_name,  sep="\t", index=True, encoding="utf-8")

if __name__ == "__main__":
    sentiment_model_name: str = "hriaz/finetuned_beliefs_sentiment_classifier_experiment1"
    input_file_name: str = "./belief_pipeline/ghana_hr_queries_beliefs_0.97_conf_with_metadata_resolved_with_just_beliefs_column.tsv"
    # Compare this output file with the one straight from the Jupyter notebook.
    output_file_name: str = "./belief_pipeline/ghana_hr_queries_beliefs_0.97_conf_with_metadata_resolved_with_just_beliefs_column-out.tsv"
    pipeline = Pipeline(
        SentimentInputStage(input_file_name),
        [
            SentimentStage(sentiment_model_name, "just_belief")
        ],
        SentimentOutputStage(output_file_name)
    )
    pipeline.run()
