from pandas import DataFrame
from pipeline import InnerStage
from transformers import pipeline

class SentimentStage(InnerStage):
    def __init__(self, model_name: str) -> None:
        super().__init__()
        self.sentiment_analysis = pipeline("sentiment-analysis", model=model_name, device="cpu")

    def run(self, data_frame: DataFrame) -> DataFrame:
        sentiment_scores = []
        for _, row in data_frame.iterrows():
            belief = row["belief"]
            sentiment_dict = self.sentiment_analysis(belief)
            label, score = sentiment_dict[0]["label"], sentiment_dict[0]["score"]
            if label == "NEGATIVE":
                score *= -1
            elif label == "UNDETERMINED":
                score = 0
            sentiment_scores.append(score)
        data_frame["sentiment_scores"] = sentiment_scores
        return data_frame
    