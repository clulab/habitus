
from pandas import DataFrame
from scenario import Scenario
from typing import List

import math
import numpy
import numpy.linalg

class SentenceMatch():

	def __init__(self, sentence_embedding, data_embedding, threshold, is_causal, is_belief) -> None:
		similarity = self.similarity(sentence_embedding, data_embedding)
		hit = threshold < similarity
		self.all   =hit and True
		self.causal=hit and is_causal
		self.belief=hit and is_belief
		self.both  =hit and is_causal and is_belief

	def similarity(self, left_embedding, right_embedding) -> float:
		result = numpy.dot(left_embedding, right_embedding) / numpy.linalg.norm(left_embedding) / numpy.linalg.norm(right_embedding)
		return result

class ChoiceMatch():
	
	def __init__(self, sentence_matches: List[SentenceMatch]) -> None:
		self.all = sum([int(sentence_match.all) for sentence_match in sentence_matches]),
		self.causal = sum([int(sentence_match.causal) for sentence_match in sentence_matches]),
		self.belief = sum([int(sentence_match.belief) for sentence_match in sentence_matches]),
		self.both = sum([int(sentence_match.both) for sentence_match in sentence_matches])

class ScenarioMatch():

	def __init__(self, choice_matches: List[ChoiceMatch]):
		all_length = numpy.linalg.norm([choice_match.all for choice_match in choice_matches])
		causal_length = numpy.linalg.norm([choice_match.causal for choice_match in choice_matches])
		belief_length = numpy.linalg.norm([choice_match.belief for choice_match in choice_matches])
		both_length = numpy.linalg.norm([choice_match.both for choice_match in choice_matches])
		self.all = [choice_match.all / all_length for choice_match in choice_matches],
		self.causal = [choice_match / causal_length for choice_match in choice_matches],
		self.belief = [choice_match / belief_length for choice_match in choice_matches],
		self.both = [choice_match / both_length for choice_match in choice_matches]

class Matcher():

	def __init__(self, sentence_transformer, data_frame: DataFrame, threshold: float) -> None:
		super().__init__()
		self.sentence_transformer = sentence_transformer
		self.data_frame = data_frame
		self.threshold = threshold
		self.data_embeddings = [sentence_transformer.encode(sentence) for sentence in data_frame["sentence"]]
		self.causal_column = self.data_frame["causal"]
		self.belief_column = self.data_frame["belief"]
	
	def match_choice(self, choice: str) -> ChoiceMatch:
		choice_embedding = self.sentence_transformer.encode(choice)
		sentence_matches = [
			SentenceMatch(
				choice_embedding,
				self.data_embeddings[index],
				bool(self.causal_column[index]),
				is_belief = bool(self.belief_column[index])
			)
			for index in range(len(self.data_frame))
		]
		choice_match = ChoiceMatch(sentence_matches)
		return choice_match

	def match_scenario(self, scenario: Scenario) -> ScenarioMatch:
		# We're not doing anything with this right now.
		self.match_sentence(scenario.introduction)
		choice_matches = [
			self.match_choice(choice)
			for choice in scenario.choices
		]
		scenario_match = ScenarioMatch(choice_matches)
		return scenario_match
