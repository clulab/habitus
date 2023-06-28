from pandas import DataFrame
from pipeline import InnerStage

class SentimentStage(InnerStage):
    def __init__(self) -> None:
        super().__init__()

    def run(self, data_frame: DataFrame) -> DataFrame:
        return data_frame
    