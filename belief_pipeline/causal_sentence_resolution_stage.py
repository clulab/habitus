from pandas import DataFrame
from pipeline import InnerStage

class CausalSentenceResolutionStage(InnerStage):
    def __init__(self) -> None:
        super().__init__()
        self.alpha_proportion_cutoff = 0.6
        self.min_sentence_length = 50
        self.max_sentence_length = 500

    def get_alpha_proportion(self, text: str) -> float:
        count = len(list(filter(lambda c: c.isalpha(), text)))
        proportion = float(count) / len(text)
        return proportion

    def is_sentence(self, text: str) -> bool:
        return text[0].isupper() and text.endswith(".") and self.get_alpha_proportion(text) >= self.alpha_proportion_cutoff
    
    def is_valid_sentence(self, sentence: str) -> bool:
        length = len(sentence)
        valid = self.min_sentence_length <= length and length <= self.max_sentence_length and self.is_sentence(sentence)
        return valid

    def resolve_sentence(self, sentence: str, index: int, sentences: list[str]) -> str:
        if self.is_valid_sentence(sentence):
            lower_sentence = sentence.lower()
            if ("they" in lower_sentence or "this" in lower_sentence) and index != 0 and self.is_sentence(sentences[index - 1]):
                resolution = f"{sentences[index - 1]} <b>{sentence}</b>"
            else:
                resolution = sentence
            return resolution
        else:
            return sentence

    def run(self, data_frame: DataFrame) -> DataFrame:
        sentences = data_frame["sentence"]
        resolutions = [self.resolve_sentence(sentence, index, sentences) for index, sentence in enumerate(sentences)]
        data_frame["sentence_resolved"] = resolutions
        return data_frame
