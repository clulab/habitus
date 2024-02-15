from pandas import DataFrame
from pipeline import InputStage

import pandas

class VectorInputStage(InputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(".")
        self.file_name = file_name

    def mk_data_frame(self, file_name: str) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", encoding="utf-8", na_values=[""], keep_default_na=False, dtype={
            "url": str,
            "sentenceIndex": int,
            "sentence": str,
            "belief": bool,
            "sent_locs": str,
            "context_locs": str
        })
        return data_frame

    def run(self) -> DataFrame:
        data_frame = self.mk_data_frame(self.file_name)
        # data_frame = data_frame[0:1000] # TODO: remove
        return data_frame
