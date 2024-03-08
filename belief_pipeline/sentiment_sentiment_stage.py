from pandas import DataFrame
from pipeline import InnerStage
from transformers import pipeline
from tqdm import tqdm

import math

class SentimentSentimentStage(InnerStage):
    def __init__(self, model_name: str, belief_column_name: str = "belief", text_column_name: str = "sentence") -> None:
        super().__init__()
        self.sentiment_analysis = pipeline("sentiment-analysis", model=model_name, device="cpu")
        self.belief_column_name = belief_column_name
        self.text_column_name = text_column_name

    def run(self, data_frame: DataFrame) -> DataFrame:
        sentiment_scores = []
        for _, row in tqdm(data_frame.iterrows(), desc=f"Calculating sentiment"):
            belief = row[self.belief_column_name]
            if belief:
                text = row[self.text_column_name]
                sentiment_dict = self.sentiment_analysis(text)
                label, score = sentiment_dict[0]["label"], sentiment_dict[0]["score"]
                if label == "NEGATIVE":
                    score *= -1
                elif label == "UNDETERMINED":
                    score = 0
            else:
                score = math.nan
            sentiment_scores.append(score)
        data_frame["sentiment_scores"] = sentiment_scores
        return data_frame
    