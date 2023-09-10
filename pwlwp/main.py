
from argparse import ArgumentParser

import openai

from matcher import Matcher
from scenario import Scenario
from sentence_transformers import SentenceTransformer
from typing import Tuple

import numpy
import pandas

def get_in_and_out() -> str: # Tuple[str, str]:
	argument_parser = ArgumentParser()
	argument_parser.add_argument("-ic", "--input-corpus", required=True, help="input corpus file name")
	argument_parser.add_argument("-iv", "--input-vector", required=True, help="input vector file name")
	args = argument_parser.parse_args()
	return args.input_corpus, args.input_vector

scenario1 = Scenario(
	"In the current Ghanaian market, a pound of gold is worth $30,000.  However, an illegal gold miner sells it for about $420. Imagine that gold is discovered in a neighboring country in even greater quantities, shifting the mining industry and causing prices to plummet in Ghana. Now, illegal miners are only receiving $200 for a pound of gold. Illegal mining has started to decline across the country. This is likely because…",
	[
		"The payoff of illegal gold mining no longer justifies the risk.",
		"Other jobs in Ghana now offer higher pay, causing those involved in illegal gold mining to seek opportunities elsewhere.",
		"Those involved in illegal gold mining are now moving abroad, where the value of gold is still higher.",
		"Gold has lost its value overall, causing Ghanaians to lose interest in the industry.",
		"None of the above."
	]
)

scenario2 = Scenario(
	"Imagine that illegal mining has become so bad in Ghana that it has become nearly impossible for vegetation to survive in the soil. This is caused by the destruction of the soil’s original nature profile as different ground layers have been mixed up over time. There is now a food shortage developing as a result of locals being unable to harvest their own food. However, illegal mining of gold continues and those struggling the most with the food crisis are also most active in the illegal mining industry. This is likely because…",
	[
		"There's a lack of education around the correlation between illegal mining and the destruction of the soil.",
		"The illegal miners find more value in the money gained through illegal mining than the vegetation they can grow.",
		"There is an assumption that the problem is temporary, and the government will step in.",
		"There were problems with the soil and food shortages prior to the illegal mining industry consuming the country, so many people are numb to the difficulties."
		"None of the above."
	]
)

scenario3 = Scenario(
	"Imagine the government makes a renewed effort to arrest illegal miners and seize and burn their equipment. However, illegal mining does not decrease, this is because:",
	[
		"There is no alternative, so miners are willing to take the risk.",
		"The security forces are taking bribes to release the arrested illegal miners.",
		"There is political interference in the prosecution of both local and foreigner illegal miners. Whenever illegal miners were arrested, there are influential local people with political connections who get them release.",
		"There is an increase in demand for gold leading to an increase in mining activity."
		"None of the above."
	]
)

def paraphrase(sentence):

	text = "Paraphrase the following sentence " + sentence + " in 5 unique ways. The answer must only have the paraphrases one per row."

	chat_completion = openai.ChatCompletion.create(model="gpt-3.5-turbo-16k",
												   messages=[{"role": "user", "content": text}])

	result = chat_completion.choices[0].message.content

	result = result.split('\n')

	crr = 0

	for str in result:
		index = 0
		for i in range(len(str)):
			if ('a' <= str[i] <= 'z') or ('A' <= str[i] <= 'Z'):
				index = i
				break
		result[crr] = str[index:]
		crr += 1

	return result


if __name__ == "__main__":

	# True if we first filter by the introduction and then choose by the choice
	filter_first = False

	# Choose if we want the sentences printed
	print_sentences = True

	# Use this if we combine the introducation and choice embeddings
	threshold = 0.6

	# Use this if we first filter by introduction and then by choice
	threshold1 = 0.3
	threshold2 = 0.6

	tokens_allowed = 3350

	if filter_first:
		threshold = threshold1

	sentence_transformer_name: str = "all-MiniLM-L6-v2"
	input_corpus_file_name: str = "../corpora/causalBeliefSentences.tsv"
	input_vector_file_name: str = "../corpora/causalBeliefSentences.npy"
	# input_corpus_file_name, input_vector_file_name = get_in_and_out()
	input_vectors = numpy.load(input_vector_file_name)
	data_frame = pandas.read_csv(input_corpus_file_name, sep="\t", encoding="utf-8", keep_default_na=False,
		dtype={"file": str, "index": int, "sentence": str, "causal": bool, "belief": bool}
	) # [:100]
	sentence_transformer = SentenceTransformer(sentence_transformer_name)

	matcher = Matcher(sentence_transformer, input_vectors, data_frame, threshold, threshold2)

	scenario_chosen = scenario1

	scenario_match = matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed)
	#scenario_match = scenario_match[4096:]

	#final_probabilities = []

	#for index in range(scenario_match.length):
#		final_probabilities.append(scenario_match.all[index] * 0.25 + scenario_match.causal[index] * 0.25 +
#								   scenario_match.belief[index] * 0.25 + scenario_match.both[index] * 0.25)


	#index_chosen = final_probabilities.index(max(final_probabilities))

	choices_str = ""

	for index in range(len(scenario_chosen.choices)):
		choices_str += str(index+1) + ") " + scenario_chosen.choices[index] + "\n\n"

	# TODO: ADD SCENARIO

	final_sentence = "You have to look carefully at the following sentences and then rank several choices based on which" \
					 "ones are most likely to be true. The sentences are: \n\n" + scenario_match + "\n\n Now rank the " \
					 "following choices based on their likelyhood while also giving intuition behind the choices from" \
					 "the context given above:\n\n" + choices_str

	openai.api_key_path = "openai_key"

	var1 = "There is political interference in the prosecution of both local and foreigner illegal miners. Whenever illegal miners were arrested, there are influential local people with political connections who get them release."
	var2 = "Other jobs in Ghana now offer higher pay, causing those involved in illegal gold mining to seek opportunities elsewhere."
	var3 = "None of the above."

	paraphrase(var2)

	#models = openai.Model.list()
	#print(str(models.data))

	#print("LEN0: " + str(len(scenario_match)))
	#print("LEN: " + str(len(final_sentence)))

	#print("THE INPUT: " + final_sentence)





	#print("We rank the choices as:\n")

	#print(chat_completion.choices[0].message.content)




	#scenario_match = matcher.match_scenario(scenario2)
	#print(scenario_match)
	#scenario_match = matcher.match_scenario(scenario3)
	#print(scenario_match)
