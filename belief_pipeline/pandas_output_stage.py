from pandas import DataFrame
from pipeline import OutputStage

class PandasOutputStage(OutputStage):
    def __init__(self, file_name: str) -> None:
        super().__init__(file_name)
        # if not os.path.exists(file_name):
        #     os.makedirs(file_name) # find the directory it's in, not use the entire file

    def log(self, message: str):
        with open("output.txt", "a", encoding="utf-8", newline="\n") as file:
            print(message, file=file)

    def write(self, text):
        nl_count = text.count("\n") + 1
        self.log(str(nl_count))
        print(str(nl_count))
        self.log(text)
        print(text, flush=True)

    # keep track of conf_threshold, coref
    def run(self, data_frame: DataFrame) -> None:
        if self.file_name:
            data_frame.to_csv(self.file_name,  sep="\t", index=False, encoding="utf-8", lineterminator="\n")
        else:
            text = data_frame.to_csv(self.file_name,  sep=",", index=False, encoding="utf-8", lineterminator="\n")
            self.write(text)
