from nltk.tokenize import sent_tokenize
from pandas import DataFrame
from pipeline import InputStage

import os

class TextDocumentRecord:
    def __init__(self, text: str) -> None:
        self.text = text

class TextSentenceRecord:
    def __init__(self, sentence: str, resolution: str, context: str) -> None:
        self.sentence = sentence
        self.resolution = resolution
        self.context = context

class TextFullRecord:
    def __init__(self, document_record: TextDocumentRecord, sentence_record: TextSentenceRecord) -> None:
        self.sentence = sentence_record.sentence
        self.resolution = sentence_record.resolution
        self.context = sentence_record.context

class TextInputStage(InputStage):
    def __init__(self, dir_name: str) -> None:
        super().__init__(dir_name)
        # These could be configured with arguments.
        self.context_window = 3
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

    def get_document(self, file_name: str) -> TextDocumentRecord:
        with open(file_name, encoding="utf-8") as file:
            lines = [line.strip() for line in file.readlines() if line.strip()] 
            text = " ".join(lines)
            return TextDocumentRecord(text)

    def tokenize_sentence(self, sentence: str, index: int, sentences: list[str]) -> TextSentenceRecord:
        lower_sentence = sentence.lower()
        if ("they" in lower_sentence or "this" in lower_sentence) and index != 0 and self.is_sentence(sentences[index - 1]):
            resolution = f"{sentences[index - 1]} <b>{sentence}</b>"
        else:
            resolution = sentence
        context_start = max(index - self.context_window, 0)
        context_end = min(index + self.context_window, len(sentences))
        context = " ".join(sentences[context_start:context_end])
        return TextSentenceRecord(sentence, resolution, context)

    def tokenize_text(self, text: str) -> list[TextSentenceRecord]:
        raw_sentences = sent_tokenize(text)
        sentences = [sentence.strip().replace("\n", " ") for sentence in raw_sentences]
        index_sentence_tuples = list(enumerate(sentences))
        valid_index_sentence_tuples = [(index, sentence) for (index, sentence) in index_sentence_tuples if self.is_valid_sentence(sentence)]
        sentence_record = [self.tokenize_sentence(sentence, index, sentences) for index, sentence in valid_index_sentence_tuples]
        return sentence_record

    def mk_data_frame(self, full_records: list[TextFullRecord]) -> DataFrame:
        na = "N/A"
        nas = [na for record in full_records]
        data_frame = DataFrame()
        data_frame["sentence"] = [record.sentence for record in full_records]
        data_frame["sentence_resolved"] = [record.resolution for record in full_records]
        data_frame["title"] = nas
        data_frame["url"] = nas
        data_frame["date"] = nas
        data_frame["byline"] = nas
        data_frame["context"] = [record.context for record in full_records]
        return data_frame

    def run(self) -> DataFrame:
        all_file_names = [os.path.join(root, file) for root, _, files in os.walk(os.path.expanduser(self.dir_name)) for file in files]
        text_file_names = list(filter(lambda string: string.endswith(".txt"), all_file_names))
        all_full_records = []
        for file_name in text_file_names:
            document_record = self.get_document(file_name)
            sentence_records = self.tokenize_text(document_record.text)
            some_full_records = [TextFullRecord(document_record, sentence_record) for sentence_record in sentence_records]
            all_full_records.extend(some_full_records)
        data_frame = self.mk_data_frame(all_full_records)
        deduplicated_data_frame = data_frame.drop_duplicates(subset=["sentence", "sentence_resolved"], inplace=False, ignore_index=True)
        return deduplicated_data_frame
