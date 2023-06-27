from pandas import DataFrame
from pipeline import InnerStage
from sentence_transformers import SentenceTransformer

import numpy

class SimilarityStage(InnerStage):
    def __init__(self) -> None:
        super().__init__()
        self.topics = "settlements, cities, towns, villages, mining, galamsey"
        self.model = SentenceTransformer("all-mpnet-base-v2")
        self.topic_vector = self.model.encode(self.topics)

    def cosine_similarity(self, v1, v2) -> float:
        return numpy.dot(v1, v2) / (numpy.linalg.norm(v1) * numpy.linalg.norm(v2))

    def rank(self, beliefs: list[str], topic: str, k: int):
        # ranking will be done based on just beliefs without the coref res context sentence (w/o previous sent)
        embeddings = self.model.encode(beliefs)
        similarities = [self.cosine_similarity(embedding, self.topic_vector) for embedding in embeddings]
        ordered_indexes = numpy.array(similarities).argsort()[-k:][::-1]
        return similarities, ordered_indexes

    def run(self, data_frame: DataFrame) -> DataFrame:
        k = len(data_frame)
        similarities, ordered_indexes = self.rank(list(data_frame["belief"]), self.topics, k)
        data_frame["similarity_score"] = similarities
        sorted_data_frame = data_frame.iloc[ordered_indexes]
        return sorted_data_frame
    