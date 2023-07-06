from location_stage import LocationStage
from pandas import DataFrame
from pipeline import OutputStage
from pipeline import Pipeline

import pandas

class LocationInputStage():
    def __init__(self, file_name: str) -> None:
        self.file_name = file_name

    def run(self) -> DataFrame:
        data_frame = pandas.read_csv(self.file_name, sep="\t", encoding="utf-8")
        return data_frame

class LocationOutputStage(OutputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(file_name)

    def run(self, data_frame: DataFrame) -> None:
        data_frame.to_csv(self.file_name,  sep="\t", index=True, encoding="utf-8")

if __name__ == "__main__":
    locations_file_name: str = "./belief_pipeline/GH.tsv"    
    input_file_name: str = "../similarity_output.tsv"
    # Compare this output file with the one straight from the Jupyter notebook.
    output_file_name: str = "../location_output.tsv"
    pipeline = Pipeline(
        LocationInputStage(input_file_name),
        [
            LocationStage(locations_file_name)
        ],
        LocationOutputStage(output_file_name)
    )
    pipeline.run()
