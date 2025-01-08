from datasets import Dataset, DatasetDict
from pandas import DataFrame
from pipeline import InnerStage
from sentence_transformers import SentenceTransformer
from tqdm import tqdm

import numpy
import torch

class VectorVectorStage(InnerStage):
    def __init__(self, model_name: str) -> None:
        super().__init__()
        self.sentence_transformer = SentenceTransformer(model_name)

    def encode(self, sentence):
        vector = self.sentence_transformer.encode(sentence)
        vector_strings = [str(value) for value in vector]
        vector_string = ", ".join(vector_strings)
        return vector_string

    def mk_vectors(self, data_frame: DataFrame):
        vectors = [self.encode(sentence) for sentence in tqdm(data_frame["sentence"])]
        return vectors

    def run(self, data_frame: DataFrame) -> DataFrame:
        vectors = self.mk_vectors(data_frame)
        data_frame["vector"] = vectors
        data_frame.drop(columns=["sentence"], inplace=True)
        return data_frame
