from datasets import Dataset, DatasetDict
from pandas import DataFrame
from pipeline import InnerStage
from transformers import AutoModelForSequenceClassification, AutoTokenizer, TrainingArguments, Trainer
from typing import Tuple

import numpy
import torch

class BeliefStage(InnerStage):
    def __init__(self, model_name: str) -> None:
        super().__init__()
        self.model_name = model_name
        transformer_name = "bert-base-cased"
        self.tokenizer = AutoTokenizer.from_pretrained(transformer_name)
        self.columns_to_keep = {"sentence", "sentence_resolved"}
        self.batch_size = 20
        self.confidence_threshold = 0.97
        self.trainer = self.mk_trainer()
        self.filter = True # False

    def mk_trainer(self) -> Trainer:
        training_args = TrainingArguments(
            output_dir="./results_triggerless",
            log_level="error",
            per_device_train_batch_size=self.batch_size,
            per_device_eval_batch_size=self.batch_size
        )
        model = AutoModelForSequenceClassification.from_pretrained(self.model_name, num_labels=len(self.columns_to_keep))
        trainer = Trainer(
            model=model,
            args=training_args,
            tokenizer=self.tokenizer
        )
        return trainer

    def tokenize_batch(self, batch):
        return self.tokenizer(batch["sentence"], truncation=True)

    def mk_dataset(self, data_frame: DataFrame, columns_to_keep: set[str]) -> Dataset:
        columns_to_remove = set(data_frame.columns) - columns_to_keep
        dataset = Dataset.from_pandas(data_frame)
        test_dataset = dataset.map(
            self.tokenize_batch,
            batched=True,
            remove_columns=columns_to_remove
        )
        return test_dataset

    def predict_with_confidence(self, dataset: Dataset):
        initial_predictions = self.trainer.predict(dataset).predictions
        
        logits = torch.from_numpy(initial_predictions)
        probabilities = torch.nn.functional.softmax(logits, dim=1)
        intermediate_predictions = torch.argmax(probabilities, dim=1)
        confidences = probabilities[range(len(intermediate_predictions)), intermediate_predictions]
        
        final_predictions = [numpy.argmax(prediction) for prediction in initial_predictions]
        return final_predictions, confidences

    def filter_beliefs(self, dataset: Dataset, data_frame: DataFrame) -> Dataset:
        if self.filter:
            predictions, confidences = self.predict_with_confidence(dataset)
            indexes = range(len(predictions))
            predicted_indexes = [index for index in indexes if predictions[index] == 1 and confidences[index] >= self.confidence_threshold]
            filtered_data_frame = data_frame.filter(items = predicted_indexes, axis=0)
            return filtered_data_frame
        else:
            return data_frame

    def run(self, data_frame: DataFrame) -> DataFrame:
        dataset = self.mk_dataset(data_frame, self.columns_to_keep)
        filtered_data_frame = self.filter_beliefs(dataset, data_frame)
        renamed_data_frame = filtered_data_frame.rename(
            columns={"sentence": "belief", "sentence_resolved": "belief_resolved"}
        )
        return renamed_data_frame