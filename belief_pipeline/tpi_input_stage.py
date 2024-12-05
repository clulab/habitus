from io import StringIO
from numbers import Number
from pandas import DataFrame
from pipeline import InputStage

import math
import pandas
import sys

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
        # Sometimes a sentence can be trimmed to empty and considered nan.
        # This is because of a mismatch in characters considered trimmable.
        data_frame["sentence"].fillna("", inplace=True)
        for index, sentence in enumerate(data_frame["sentence"]):
            if sentence == "": # or (isinstance(sentence, Number) and math.isnan(sentence)):
                print("There is an empty sentence!")
                data_frame["sentence"][index] = "" # What should be done?
        return data_frame
    
    def read(self) -> StringIO:
        # In Python, the line separator is preserved.
        nl_count = int(sys.stdin.readline().strip())
        buffer = StringIO()
        for i in range(0, nl_count):
            line = sys.stdin.readline()
            if i + 1 < nl_count:
                buffer.write(line)
            else:
                # Remove trailing NL from last line.
                buffer.write(line[:-1])
        return buffer

    def run(self) -> DataFrame:
        if self.file_name:
            source = self.file_name
        else:
            source = self.read()
        data_frame = self.mk_data_frame(source)
        # data_frame = data_frame[0:1000] # TODO: remove
        return data_frame
