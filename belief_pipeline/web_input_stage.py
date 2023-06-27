from nltk.tokenize import sent_tokenize
from pandas import DataFrame
from pipeline import InputStage

import json
import os

class WebDocumentRecord:
    def __init__(self, url: str, title: str, dateline: str, byline: str, text: str) -> None:
        na = "N/A"
        self.url = url
        self.title = title
        self.dateline = dateline if dateline is not None else na
        self.byline = byline if byline is not None else na
        self.text = text

class WebSentenceRecord:
    def __init__(self, sentence: str, resolution: str, context: str) -> None:
        self.sentence = sentence
        self.resolution = resolution
        self.context = context

class WebFullRecord:
    def __init__(self, document_record: WebDocumentRecord, sentence_record: WebSentenceRecord) -> None:
        self.url = document_record.url
        self.title = document_record.title
        self.dateline = document_record.dateline
        self.byline = document_record.byline
        self.sentence = sentence_record.sentence
        self.resolution = sentence_record.resolution
        self.context = sentence_record.context

class WebInputStage(InputStage):
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

    def get_str_else_none(self, dict: dict[str, str], key: str) -> str:
        return str(dict[key]) if key in dict else None

    def get_document(self, file_name: str) -> WebDocumentRecord:
        with open(file_name, encoding="utf-8") as file:
            data = json.load(file)
            url = str(data["url"])
            title = str(data["title"])
            dateline = self.get_str_else_none(data, "dateline")
            byline = self.get_str_else_none(data, "byline")
            text = str(data["text"]).replace("\n", " ")
            return WebDocumentRecord(url, title, dateline, byline, text)

    def tokenize_sentence(self, sentence: str, index: int, sentences: list[str]) -> WebSentenceRecord:
        lower_sentence = sentence.lower()
        if ("they" in lower_sentence or "this" in lower_sentence) and index != 0 and self.is_sentence(sentences[index - 1]):
            resolution = f"{sentences[index - 1]} <b>{sentence}</b>"
        else:
            resolution = sentence
        context_start = max(index - self.context_window, 0)
        context_end = min(index + self.context_window, len(sentences))
        context = " ".join(sentences[context_start:context_end])
        return WebSentenceRecord(sentence, resolution, context)

    def tokenize_text(self, text: str) -> list[WebSentenceRecord]:
        raw_sentences = sent_tokenize(text)
        sentences = [sentence.strip().replace("\n", " ") for sentence in raw_sentences]
        index_sentence_tuples = list(enumerate(sentences))
        valid_index_sentence_tuples = [(index, sentence) for (index, sentence) in index_sentence_tuples if self.is_valid_sentence(sentence)]
        sentence_record = [self.tokenize_sentence(sentence, index, sentences) for index, sentence in valid_index_sentence_tuples]
        return sentence_record

    def mk_data_frame(self, full_records: list[WebFullRecord]) -> DataFrame:
        data_frame = DataFrame()
        data_frame["sentence"] = [record.sentence for record in full_records]
        data_frame["sentence_resolved"] = [record.resolution for record in full_records]
        data_frame["title"] = [record.title for record in full_records]
        data_frame["url"] = [record.url for record in full_records]
        data_frame["date"] = [record.dateline for record in full_records]
        data_frame["byline"] = [record.byline for record in full_records]
        data_frame["context"] = [record.context for record in full_records]
        return data_frame

    def run(self) -> DataFrame:
        all_file_names = [os.path.join(root, file) for root, _, files in os.walk(os.path.expanduser(self.dir_name)) for file in files]
        json_file_names = list(filter(lambda string: string.endswith(".json"), all_file_names))
        all_full_records = []
        for file_name in json_file_names:
            document_record = self.get_document(file_name)
            sentence_records = self.tokenize_text(document_record.text)
            some_full_records = [WebFullRecord(document_record, sentence_record) for sentence_record in sentence_records]
            all_full_records.extend(some_full_records)
        data_frame = self.mk_data_frame(all_full_records)
        deduplicated_data_frame = data_frame.drop_duplicates(subset=["sentence", "sentence_resolved"], inplace=False, ignore_index=True)
        return deduplicated_data_frame
