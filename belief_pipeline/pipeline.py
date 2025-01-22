
from pandas import DataFrame

class PipelineStage():
    def _init__(self) -> None:
        pass

    def log(self, message: str):
        self.logToFile("log.txt", message)

    def logToFile(self, filename: str, message: str):
        with open(filename, "a", encoding="utf-8", newline="\n") as file:
            print(message, file=file)

class OuterStage(PipelineStage):
    def __init__(self) -> None:
        super().__init__()

class InputStage(OuterStage):
    def __init__(self, dir_name: str) -> None:
        super().__init__()
        self.dir_name = dir_name

    def run(self) -> DataFrame:
        pass

    def log(self, message: str):
        self.logToFile("input.txt", message)

class OutputStage(OuterStage):
    def __init__(self, file_name: str) -> None:
        super().__init__()
        self.file_name = file_name

    def run(self, data_frame: DataFrame):
        pass

    def log(self, message: str):
        self.logToFile("output.txt", message)

class InnerStage(PipelineStage):
    def __init__(self) -> None:
        super().__init__()

    def run(self, input_data_frame: DataFrame, output_data_frame: DataFrame):
        pass

class Pipeline():
    def __init__(self, input_stage: InputStage, inner_stages: list[InnerStage], output_stage: OutputStage) -> None:
        self.input_stage = input_stage
        self.inner_stages = inner_stages
        self.output_stage = output_stage
        
    def run(self) -> None:
        data_frame = self.input_stage.run()
        for inner_stage in self.inner_stages:
            data_frame = inner_stage.run(data_frame) 
        self.output_stage.run(data_frame)
        