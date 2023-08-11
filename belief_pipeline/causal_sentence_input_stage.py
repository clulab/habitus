from pandas import DataFrame
from pipeline import InputStage

import pandas

class CausalSentenceInputStage(InputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(".")
        self.file_name = file_name

    def mk_data_frame(self, file_name: str) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", encoding="utf-8", keep_default_na=False,
            dtype={"file": str, "index": int, "sentence": str, "causal": bool}
        )
        return data_frame

    def run(self) -> DataFrame:
        data_frame = self.mk_data_frame(self.file_name)
        # data_frame = data_frame[0:10] # TODO: remove
        return data_frame
