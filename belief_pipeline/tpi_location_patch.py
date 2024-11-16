from pandas import DataFrame
from pipeline import InnerStage
# from tqdm import tqdm

# import itertools
import pandas
import re
import spacy

import sys

class TextWithIndices():
    def __init__(self, text, indices=None):
        super().__init__()
        self.text = text
        if indices == None:
            self.indices = [index for index, value in enumerate(text)]
        else:
            self.indices = indices

    def split(self, separator: str) -> list["TextWithIndices"]:
        parts = self.text.split(separator)
        textWithIndicesList = []
        offset = 0
        for part in parts:
            textWithIndices = TextWithIndices(part, self.indices[offset:offset + len(part)])
            textWithIndicesList.append(textWithIndices)
            offset += len(part)
            offset += len(separator)
        return textWithIndicesList

    def re_sub(self, pattern: str, repl: str) -> "TextWithIndices":
        done = False
        text = self.text
        indices = self.indices
        while not done:
            match = re.search(pattern, text)
            if match == None:
                done = True
            else:
                # The indices must be done before the text gets changed.
                indices = indices[0:match.start()] + ([-1] * len(repl)) + indices[match.end():len(text)]
                text = text[0:match.start()] + repl + text[match.end():len(text)]
        return TextWithIndices(text, indices)


class Location():
    def __init__(self, textWithIndices: TextWithIndices, lat: float, lon: float, canonical: str, geonameid: str):
        # Make sure indices are reasonable.
        if -1 in textWithIndices.indices:
            print("There is a -1 among the indices!")
        for index, offset in enumerate(textWithIndices.indices):
            if offset != textWithIndices.indices[0] + index:
                print("The indices are not consecutive!")    
        self.textWithIndices = textWithIndices
        self.lat = lat
        self.lon = lon
        self.canonical = canonical
        self.geonameid = geonameid

    def __str__(self):
        return f"{self.textWithIndices.text}\t{self.canonical}\t{self.geonameid}\t{self.textWithIndices.indices[0]}\t{self.textWithIndices.indices[-1] + 1}\t{self.lat}\t{self.lon}"

class LocationsPatch():
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
        geonameids = locations_data_frame["geonameid"]
        self.names_to_canonical = self.get_names_to_values(names, ascii_names, alternate_names, names)
        self.names_to_latitudes = self.get_names_to_values(names, ascii_names, alternate_names, latitudes)
        self.names_to_longitudes = self.get_names_to_values(names, ascii_names, alternate_names, longitudes)
        self.names_to_geonameids = self.get_names_to_values(names, ascii_names, alternate_names, geonameids)
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

    def add_location(self, locations: list[Location], entityWithIndices: TextWithIndices) -> None:
        # NER includes `the` in locations, but the location database file doesn't.
        # TODO: what should be done if this is not the case?  There are some instances
        # in the database.  Should both be checked?  Yes, they should indeed.
        # However, they were not in the initial pass and we don't want to find locations
        # that weren't there before, because that would necessitate reading every sentence
        # just in case there was a new one.
        # The canonical name should be the one in the database.
        # Let's not find any new ones with this.
        # entityWithIndices = entityWithIndices.re_sub("^[Tt]he\s", "")
        entityWithIndices = entityWithIndices.re_sub("^the\s", "")
        if entityWithIndices.text in self.names_to_latitudes and not entityWithIndices.text in self.common_words and entityWithIndices.text[0].isupper():
            location = Location(entityWithIndices, self.names_to_latitudes[entityWithIndices.text], self.names_to_longitudes[entityWithIndices.text], self.names_to_canonical[entityWithIndices.text], self.names_to_geonameids[entityWithIndices.text])
            locations.append(location)



    def get_cell_locations(self, textWithIndices: TextWithIndices) -> list[Location]:
        # Replace author citations where possible because they sometimes get labeled as locations.
        textWithIndices = textWithIndices.re_sub("[A-Z][a-z]+,\s\d\d\d\d", "(Author, year)")
        entities = self.NER(textWithIndices.text).ents
        non_persons = [entity for entity in entities if entity.label_ != "PERSON"]
        locations = []
        for entity in non_persons:
            entitiesWithIndices = TextWithIndices(str(entity), textWithIndices.indices[entity.start_char:entity.end_char])
            entityWithIndicesList = entitiesWithIndices.split(", ")
            for entityWithIndices in entityWithIndicesList:
                self.add_location(locations, entityWithIndices)
        # TODO: Change the next line
        return locations # sorted(list(set(locations)))
    
    def patch(self, sentence: str) -> list[Location]:
        return self.get_cell_locations(TextWithIndices(sentence))
    
def run():
    sys.stdin.reconfigure(encoding="utf-8")

    # Make sure the sentence is from the right dataset
    locationsPatch = LocationsPatch("./belief_pipeline/GH.tsv")

    # Read input until EOF
    while True:
        line = sys.stdin.readline().strip()

        with open("debug.txt", "a") as file:
            print(line, file=file)
            locations = locationsPatch.patch(line)
            for location in locations:
                print(location, file=file)
                print(location)
            print("", flush=True)

def test():
    locationsPatch = LocationsPatch("./belief_pipeline/GH.tsv")
    # sentence = "Where in Ghana is the town of Zugu, for the town of Zugu is unknown to me?" # This only gets the first.
    # sentence = "Lots of people live in Accra when I don't even know where Accra is located." # This also only gets the first.
    # sentence = "Do more people live in Accra or in Trume?" # This gets both.
    # sentence = "More people live in Accra than in Trume." # This gets both.
    # sentence = "More people live in Accra, Trume, Krasa, and Benu than in Damsa." # This misses Krasa.
    # sentence = "According to Smith, 2024, more people live in Accra, the Trume, Krasa, and Benu than in Damsa." # This misses Krasa.
    # sentence = "According to Smith, 2024, more people live in Accra, the Trume, Krasa, and Benu than in Damsa." # This misses Krasa.
    # sentence = "According to Smith, 2024, more people live in Yabonzue, the Tampielim, London, and Benu than in Damsa."
    # sentence = "The Bia River serves as a vital source of water for the residents of Bianouan in eastern Ivory Coast."
    # sentence = "Ivory Coast’s semi-public water distribution company, SODECI, recently shut down its water treatment plant in the area because of the level of pollution in the Bia River."
    # sentence = "According to Smith, 2024 and Jackson, 2022 more people live in Yabonzue, the Tampielim, London, and Benu than in Damsa."
    # sentence = "We are staying at The Aknac Hotel in Ghana."
    # sentence = "We are staying at the Aknac Hotel in Ghana."
    sentence = "How many people live in the Volta part of Ghana?"
    locations = locationsPatch.patch(sentence)
    for location in locations:
        print(sentence, location)


if __name__ == "__main__":
    run()
    # test()
