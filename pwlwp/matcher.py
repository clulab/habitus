
from pandas import DataFrame
from scenario import Scenario
from typing import List

import numpy
import numpy.linalg

class SentenceMatch():

	choice_str = ""

	def __init__(self, choice_str, sentence_embedding, data_embedding, threshold: float, is_causal: bool, is_belief: bool) -> None:
		similarity = self.similarity(sentence_embedding, data_embedding)
		hit = threshold < similarity
		self.all   =hit and True
		self.causal=hit and is_causal
		self.belief=hit and is_belief
		self.both  =hit and is_causal and is_belief
		self.choice_str = choice_str

	def similarity(self, left_embedding, right_embedding) -> float:
		result = numpy.dot(left_embedding, right_embedding) / numpy.linalg.norm(left_embedding) / numpy.linalg.norm(right_embedding)
		return result

class SentenceMatchNoChoice():

	choice_str = ""

	def __init__(self, introduction_embedding, data_text, data_embedding, threshold1: float, is_causal: bool, is_belief: bool) -> None:
		similarity1 = self.similarity(introduction_embedding, data_embedding)
		#print("? " + str(threshold1))
		hit = threshold1 < similarity1
		self.is_hit = hit
		self.all   =hit and True
		self.causal=hit and is_causal
		self.belief=hit and is_belief
		self.both  =hit and is_causal and is_belief
		self.data_text = data_text

	def similarity(self, left_embedding, right_embedding) -> float:
		result = numpy.dot(left_embedding, right_embedding) / numpy.linalg.norm(left_embedding) / numpy.linalg.norm(right_embedding)
		return result


class SentenceMatchFilter():

	choice_str = ""

	def __init__(self, choice_str, introduction_embedding, sentence_embedding, data_embedding, threshold1: float, threshold2: float, is_causal: bool, is_belief: bool) -> None:
		similarity1 = self.similarity(introduction_embedding, data_embedding)
		pass_first = threshold1 < similarity1
		similarity2 = self.similarity(sentence_embedding, data_embedding)
		hit = pass_first and (threshold2 < similarity2)
		self.all   =hit and True
		self.causal=hit and is_causal
		self.belief=hit and is_belief
		self.both  =hit and is_causal and is_belief
		self.choice_str = choice_str

	def similarity(self, left_embedding, right_embedding) -> float:
		result = numpy.dot(left_embedding, right_embedding) / numpy.linalg.norm(left_embedding) / numpy.linalg.norm(right_embedding)
		return result

class ChoiceMatch():
	
	def __init__(self, sentence_matches: List[SentenceMatch]) -> None:
		self.all    = sum([int(sentence_match.all)    for sentence_match in sentence_matches])
		self.causal = sum([int(sentence_match.causal) for sentence_match in sentence_matches])
		self.belief = sum([int(sentence_match.belief) for sentence_match in sentence_matches])
		self.both   = sum([int(sentence_match.both)   for sentence_match in sentence_matches])

	def __str__(self) -> str:
		header = "all\tcausal\tbelief\tboth"
		line = f"{self.all}\t{self.causal}\t{self.belief}\t{self.both}"
		string = header + ("\n") + line
		return string

class ScenarioMatch():

	def __init__(self, choice_matches: List[ChoiceMatch]) -> None:
		def quotient_or_zero(numerator, denominator) -> float:
			if denominator:
				return numerator / denominator
			else:
				return 0.0
			
		self.choice_matches = choice_matches
		self.length = len(choice_matches)
		all_length    = sum([choice_match.all    for choice_match in choice_matches])
		causal_length = sum([choice_match.causal for choice_match in choice_matches])
		belief_length = sum([choice_match.belief for choice_match in choice_matches])
		both_length   = sum([choice_match.both   for choice_match in choice_matches])
		self.all    = [quotient_or_zero(choice_match.all,    all_length)    for choice_match in choice_matches]
		self.causal = [quotient_or_zero(choice_match.causal, causal_length) for choice_match in choice_matches]
		self.belief = [quotient_or_zero(choice_match.belief, belief_length) for choice_match in choice_matches]
		self.both   = [quotient_or_zero(choice_match.both,   both_length)   for choice_match in choice_matches]

	def __str__(self) -> str:
		[print(choice_match) for choice_match in self.choice_matches]

		header = "index\tall\tcausal\tbelief\tboth\n"
		lines = [
			f"{index}\t{self.all[index]}\t{self.causal[index]}\t{self.belief[index]}\t{self.both[index]}"
			for index in range(self.length)
		]
		string = header + ("\n").join(lines)
		return string
	
class Matcher():

	def __init__(self, sentence_transformer, data_embeddings, data_frame: DataFrame, threshold: float, threshold2: float) -> None:
		super().__init__()
		self.sentence_transformer = sentence_transformer
		self.data_frame = data_frame
		self.threshold = threshold
		self.threshold2 = threshold2
		self.data_embeddings = data_embeddings
		self.causal_column = self.data_frame["causal"]
		self.belief_column = self.data_frame["belief"]
	
	def match_choice(self, introduction: str, choice: str, to_print: bool, filter_first: bool) -> ChoiceMatch:

		sentence_matches = []

		if not filter_first:

			initial_embedding = self.sentence_transformer.encode(choice)
			introduction_embedding = self.sentence_transformer.encode(introduction)
			choice_embedding = numpy.add(initial_embedding, introduction_embedding) / 2

			sentence_matches = [
				SentenceMatch(
					choice,
					choice_embedding,
					self.data_embeddings[index],
					self.threshold,
					is_causal=bool(self.causal_column[index]),
					is_belief=bool(self.belief_column[index])
				)
				for index in range(len(self.data_frame))
			]

		else:

			initial_embedding = self.sentence_transformer.encode(choice)
			introduction_embedding = self.sentence_transformer.encode(introduction)

			sentence_matches = [
				SentenceMatchFilter(
					choice,
					introduction_embedding,
					initial_embedding,
					self.data_embeddings[index],
					self.threshold,
					self.threshold2,
					is_causal=bool(self.causal_column[index]),
					is_belief=bool(self.belief_column[index])
				)
				for index in range(len(self.data_frame))
			]

		if to_print:
			print("Causal matches: ")
			print("")

			for index in range(len(self.data_frame)):
				if sentence_matches[index].causal:
					print(self.data_frame["sentence"][index])
					print("")

			print("")
			print("")
			print("")
			print("Belief matches: ")

			for index in range(len(self.data_frame)):
				if sentence_matches[index].belief:
					print(self.data_frame["sentence"][index])
					print("")
			print("")
			print("")
			print("")
			print("Both matches: ")

			for index in range(len(self.data_frame)):
				if sentence_matches[index].both:
					print(self.data_frame["sentence"][index])
					print("")

		choice_match = ChoiceMatch(sentence_matches)
		return choice_match

	def match_scenario(self, scenario: Scenario, to_print: bool, filter_first: bool) -> ScenarioMatch:

		if to_print:
			print("Now matching: " + scenario.introduction)
			print("")

		#choice_matches = []
		#for choice in scenario.choices:
	#		if to_print:
	#			print("Looking at choice: " + str(choice))
#				print("")
		#	choice_matches.append(self.match_choice(scenario.introduction, choice, to_print, filter_first))

		introduction_embedding = self.sentence_transformer.encode(scenario.introduction)

		sentence_matches = []

		for index in range(len(self.data_frame)):
			sentence_match = SentenceMatchNoChoice(
				introduction_embedding,
				self.data_frame["sentence"][index],
				self.data_embeddings[index],
				self.threshold,
				is_causal=bool(self.causal_column[index]),
				is_belief=bool(self.belief_column[index])
			)
			if sentence_match.is_hit:
				sentence_matches.append(sentence_match.data_text)

		#scenario_match = ScenarioMatch(scenario)
		return "\n\n".join(sentence_matches)
