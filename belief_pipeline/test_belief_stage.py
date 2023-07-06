from belief_stage import BeliefStage
from pandas_output_stage import PandasOutputStage
from pipeline import Pipeline
from web_input_stage import WebInputStage

if __name__ == "__main__":
    belief_model_name: str = "maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34"
    # Compare this output file with the one straight from the Jupyter notebook.
    input_dir_name: str = "../corpora/Galamsey-web-435"
    output_file_name: str = "../belief_output.tsv"
    pipeline = Pipeline(
        WebInputStage(input_dir_name),
        [
            BeliefStage(belief_model_name),
        ],
        PandasOutputStage(output_file_name)
    )
    pipeline.run()
