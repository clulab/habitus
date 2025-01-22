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

    def mk_data_frame(self, file_name: str, sep: str) -> DataFrame:
        data_frame = pandas.read_csv(file_name, sep=sep, encoding="utf-8", na_values=[""], keep_default_na=False, dtype={
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
        # data_frame["prevSentence"].fillna("", inplace=True)
        # data_frame["prevSentence"] = data_frame["prevSentence"].fillna("")
        data_frame.fillna({"prevSentence": ""}, inplace=True)
        # Sometimes a sentence can be trimmed to empty and considered nan.
        # This is because of a mismatch in characters considered trimmable.
        # data_frame["sentence"].fillna("", inplace=True)
        # data_frame["sentence"] = data_frame["sentence"].fillna("")
        data_frame.fillna({"sentence": ""}, inplace=True)
        for index, sentence in enumerate(data_frame["sentence"]):
            if sentence == "": # or (isinstance(sentence, Number) and math.isnan(sentence)):
                print("There is an empty sentence!")
                data_frame["sentence"][index] = "" # What should be done?
        return data_frame

    def read(self) -> StringIO:
        # In Python, the line separator is preserved.
        line = sys.stdin.readline()
        self.log(line)
        nl_count = int(line)
        buffer = StringIO()
        for i in range(0, nl_count):
            line = sys.stdin.readline()
            self.log(line)
            if i + 1 < nl_count:
                buffer.write(line)
            else:
                # Remove trailing NL from last line.
                buffer.write(line[:-1])
        value = buffer.getvalue()
        return StringIO(value)
    
    def read2(self) -> StringIO:
        with open(self.file_name, "r", encoding="utf-8") as file:
            file_content = file.read()
        return StringIO(file_content)

    def run(self) -> DataFrame:
        if self.file_name:
            source = self.file_name
            sep = "\t"
            # source = self.read2()
            # sep = ","
        else:
            source = self.read()
            sep = ","
        data_frame = self.mk_data_frame(source, sep)
        # data_frame = data_frame[0:1000] # TODO: remove
        return data_frame
