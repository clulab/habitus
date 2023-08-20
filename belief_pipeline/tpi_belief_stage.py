from datasets import Dataset, DatasetDict
from pandas import DataFrame
from pipeline import InnerStage
from transformers import AutoModelForSequenceClassification, AutoTokenizer, TrainingArguments, Trainer

import numpy
import torch

class TpiBeliefStage(InnerStage):
    def __init__(self, model_name: str) -> None:
        super().__init__()
        self.model_name = model_name
        transformer_name = "bert-base-cased"
        self.tokenizer = AutoTokenizer.from_pretrained(transformer_name)
        self.columns_to_keep = {"sentence", "sentence_resolved"}
        self.batch_size = 20
        self.confidence_threshold = 0.97
        self.trainer = self.mk_trainer()

    def mk_trainer(self) -> Trainer:
        training_args = TrainingArguments(
            output_dir="./results_triggerless",
            log_level="error",
            per_device_train_batch_size=self.batch_size,
            per_device_eval_batch_size=self.batch_size
        )
        model = AutoModelForSequenceClassification.from_pretrained(self.model_name, local_files_only=True, num_labels=len(self.columns_to_keep))
        model = model.to("cpu")
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

    def mk_beliefs(self, dataset: Dataset):
        predictions, confidences = self.predict_with_confidence(dataset)
        indexes = range(len(predictions))
        beliefs = [predictions[index] == 1 and confidences[index] >= self.confidence_threshold for index in indexes]
        beliefs = [bool(belief) for belief in beliefs]
        return beliefs

    def run(self, data_frame: DataFrame) -> DataFrame:
        dataset = self.mk_dataset(data_frame, self.columns_to_keep)
        beliefs = self.mk_beliefs(dataset)
        data_frame["belief"] = beliefs
        data_frame.drop(columns=["prevSentence", "sentence_resolved"], inplace=True)
        return data_frame
