from pandas import DataFrame
from pipeline import InputStage

import pandas

class TpiInputStage(InputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(".")
        self.file_name = file_name

    def mk_data_frame(self, file_name: str) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", encoding="utf-8", na_values=[""], keep_default_na=False, dtype={
            "url": str,
            "terms": str,
            "date": str,
            "sentenceIndex": int,
            "sentence": str,
            "context": str,
            "causal": bool,
            "causalIndex": "Int32", # this allows for None
            "negationCount": "Int32",

            "causeIncCount": "Int32",
            "causeDecCount": "Int32",
            "causePosCount": "Int32",
            "causeNegCount": "Int32",
            
            "effectIncCount": "Int32",
            "effectDecCount": "Int32",
            "effectPosCount": "Int32",
            "effectNegCount": "Int32",

            "causeText": str,
            "effectText": str,
            "prevSentence": str
        })
        data_frame["prevSentence"].fillna("", inplace=True)
        return data_frame

    def run(self) -> DataFrame:
        data_frame = self.mk_data_frame(self.file_name)
        data_frame = data_frame[0:1000] # TODO: remove
        return data_frame
