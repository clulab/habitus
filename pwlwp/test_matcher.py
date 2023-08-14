
from matcher import ChoiceMatch, ScenarioMatch, SentenceMatch
from scenario import Scenario
from sentence_transformers import SentenceTransformer

import math

scenario = Scenario(
	"introduction",
	[
		"The payoff of illegal gold mining no longer justifies the risk.",
		"Other jobs in Ghana now offer higher pay, causing those involved in illegal gold mining to seek opportunities elsewhere.",
		"Those involved in illegal gold mining are now moving abroad, where the value of gold is still higher.",
		"Gold has lost its value overall, causing Ghanaians to lose interest in the industry."
	]
)

tolerance = 0.000001
vector = [1.0, 0.0]
parallel = [1.0, 0.0]
orthogonal = [0.0, 1.0]
threshold = 0.8

def test_sentence_transformer():
	sentence_transformer_name: str = "all-distilroberta-v1"
	sentence_transformer = SentenceTransformer(sentence_transformer_name)
	encoding = sentence_transformer.encode(scenario.choices[0])
	sentence_match = SentenceMatch(encoding, encoding, threshold, True, True)
	similarity = sentence_match.similarity(encoding, encoding)
	assert abs(similarity - 1.0) < tolerance

def test_sentence_match():
	sentence_match = SentenceMatch(vector, parallel, threshold, True, True)
	assert sentence_match.all
	assert sentence_match.causal
	assert sentence_match.belief
	assert sentence_match.both

	sentence_match = SentenceMatch(vector, orthogonal, threshold, True, True)
	assert not sentence_match.all
	assert not sentence_match.causal
	assert not sentence_match.belief
	assert not sentence_match.both

	sentence_match = SentenceMatch(vector, parallel, threshold, True, False)
	assert sentence_match.all
	assert sentence_match.causal
	assert not sentence_match.belief
	assert not sentence_match.both

def test_choice_match():
	sentence_matches = [
		SentenceMatch(vector, parallel, threshold, False, False),
		SentenceMatch(vector, parallel, threshold, False, True),
		SentenceMatch(vector, parallel, threshold, True,  False),
		SentenceMatch(vector, parallel, threshold, True,  True),

		SentenceMatch(vector, parallel, threshold, True,  False)
	]
	choice_match = ChoiceMatch(sentence_matches)
	assert choice_match.all == 5
	assert choice_match.causal == 3
	assert choice_match.belief == 2
	assert choice_match.both == 1

def test_scenario_match():
	choice_matches = [
		ChoiceMatch([
			SentenceMatch(vector, parallel, threshold, False, False),
			SentenceMatch(vector, parallel, threshold, True,  False)
		]), # 2, 1, 0, 0
		ChoiceMatch([
			SentenceMatch(vector, parallel, threshold, True,  True),
			SentenceMatch(vector, parallel, threshold, False, True),
			SentenceMatch(vector, parallel, threshold, True,  False)
		])  # 3, 2, 2, 1
	]
	scenario_match = ScenarioMatch(choice_matches)
	assert scenario_match.all == [2 / 5, 3 / 5]
	assert scenario_match.causal == [1 / 3, 2 / 3]
	assert scenario_match.belief == [0.0, 1.0]
	assert scenario_match.both == [0.0, 1.0]

if __name__ == "__main__":
	test_sentence_transformer()
	test_sentence_match()
	test_choice_match()
	test_scenario_match()
