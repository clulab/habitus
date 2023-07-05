from pandas import DataFrame
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from similarity_stage import SimilarityStage

import pandas

class SimilarityInputStage():
    def __init__(self, file_name: str) -> None:
        self.file_name = file_name

    def run(self) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", header=0, names=["belief", "belief_resolved", "title", "url", "date", "byline", "context"])
        return data_frame

if __name__ == "__main__":
    input_file_name: str = "../belief_output.tsv"
    # Compare this output file with the one straight from the Jupyter notebook.
    output_file_name: str = "../similarity_output.tsv"
    pipeline = Pipeline(
        SimilarityInputStage(input_file_name),
        [
            SimilarityStage()
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
