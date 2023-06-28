from pandas import DataFrame

# import os

from pipeline import OutputStage

class PandasOutputStage(OutputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(file_name)
        # if not os.path.exists(file_name):
        #     os.makedirs(file_name) # find the directory it's in, not use the entire file

    # keep track of conf_threshold, coref
    def run(self, data_frame: DataFrame) -> None:
        data_frame.to_csv(self.file_name,  sep="\t", index=False, encoding="utf-8")
