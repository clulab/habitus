from pandas import DataFrame
from pipeline import InnerStage
from tqdm import tqdm

import itertools
import pandas
import re
import spacy

class TpiLocationStage(InnerStage):
    def __init__(self, locations_file_name: str) -> None:
        super().__init__()
        # The Uganda locations file has an extra column of notes, but we are not using it, so it is not included
        # in order to keep the code compatible with the Ghana locations.  The extra column causes confusion with
        # identification of the index column, so that is explicitly turned off here.  You will see a warning
        # message on the console about lost data, probably from the extra column that we're not using here:
        # ParserWarning: Length of header or names does not match length of data. This leads to a loss of data
        # with index_col=False.
        locations_data_frame = pandas.read_csv(locations_file_name, sep="\t", encoding="utf-8", index_col=False, names=[
            "geonameid", "name", "asciiname", "alternatenames", "latitude", "longitude", "unk1", "unk2", "country_code",
            "cc2", "unk3", "unk4", "unk5", "unk6", "population", "elevation", "unk7", "timezone", "unk8" #, "notes"
        ])
        names = locations_data_frame["name"]
        ascii_names = locations_data_frame["asciiname"]
        alternate_names = locations_data_frame["alternatenames"]
        latitudes = locations_data_frame["latitude"]
        longitudes = locations_data_frame["longitude"]
        self.names_to_latitudes = self.get_names_to_values(names, ascii_names, alternate_names, latitudes)
        self.names_to_longitudes = self.get_names_to_values(names, ascii_names, alternate_names, longitudes)
        # These are real locations in the ghana locations file from geonames (https://download.geonames.org/export/dump/),
        # but they also happen to be frequent English words, so we exclude them from being identified as locations.
        self.common_words = ["We", "No", "To", "Some"]
        self.NER = NER = spacy.load("en_core_web_sm")

    def get_names_to_values(self, names: list[str], ascii_names: list[str], alternate_names: list[str], values: list[str]) -> dict[str, str]:
        # In case there are duplicates, these are read from lowest to highest priority, so that the lower ones are overwritten.
        names_to_values = {}
        for name, value in zip(alternate_names, values):
            if type(name) is str:
                split = name.split(",")
                for item in split:
                    names_to_values[item] = value
        for name, value in zip(names, values):
            names_to_values[name] = value
        for name, value in zip(ascii_names, values):
            names_to_values[name] = value
        return names_to_values

    def add_location(self, locations: list[str], entity: str) -> None:
        # NER includes `the` in locations, but the location database file doesn't.
        entity = re.sub("^the\s", "", entity)
        if entity in self.names_to_latitudes and not entity in self.common_words and entity[0].isupper():
            location = f"{entity} ({self.names_to_latitudes[entity]}, {self.names_to_longitudes[entity]})"
            locations.append(location)

    def get_cell_locations(self, sentence: str) -> list[str]:
        # Replace author citations where possible because they sometimes get labeled as locations.
        sentence = re.sub("[A-Z][a-z]+,\s\d\d\d\d", "(Author, year)", sentence)
        entities = self.NER(sentence).ents
        non_persons = [str(entity) for entity in entities if entity.label_ != "PERSON"]
        locations = []
        for entity in non_persons:
            # Sometimes two comma-separated locations are found as one location.
            split = entity.split(", ")
            for entity in split:
                self.add_location(locations, entity)
        return sorted(list(set(locations)))

    def get_column_locations(self, strings: list[str], desc: str) -> list[str]:
        locations = [self.get_cell_locations(string) for string in tqdm(strings, desc=f"Finding {desc} locations")]
        joined_locations = [", ".join(location) for location in locations]
        return joined_locations

    def run(self, data_frame: DataFrame) -> DataFrame:
        sentence_locations = self.get_column_locations(data_frame["sentence"], "sentence")
        context_locations = self.get_column_locations(data_frame["context"], "context")
        data_frame["sent_locs"] = sentence_locations
        data_frame["context_locs"] = context_locations   
        data_frame.drop(columns=["context"], inplace=True)
        return data_frame
