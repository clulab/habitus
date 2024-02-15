from datasets import Dataset, DatasetDict
from pandas import DataFrame
from pipeline import InnerStage
from sentence_transformers import SentenceTransformer

import numpy
import torch

class VectorVectorStage(InnerStage):
    def __init__(self, model_name: str) -> None:
        super().__init__()
        self.sentence_transformer = SentenceTransformer(model_name)

    def encode(self, index, sentence):
        print(index)
        vector = self.sentence_transformer.encode(sentence)
        vector_strings = [str(value) for value in vector]
        vector_string = ", ".join(vector_strings)
        return vector_string

    def mk_vectors(self, data_frame: DataFrame):
        vectors = [self.encode(index, sentence) for index, sentence in enumerate(data_frame["sentence"])]
        return vectors

    def run(self, data_frame: DataFrame) -> DataFrame:
        vectors = self.mk_vectors(data_frame)
        data_frame["vector"] = vectors
        return data_frame
