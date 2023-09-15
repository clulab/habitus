
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
		"There were problems with the soil and food shortages prior to the illegal mining industry consuming the country, so many people are numb to the difficulties.",
		"None of the above."
	]
)

scenario3 = Scenario(
	"Imagine the government makes a renewed effort to arrest illegal miners and seize and burn their equipment. However, illegal mining does not decrease, this is because:",
	[
		"There is no alternative, so miners are willing to take the risk.",
		"The security forces are taking bribes to release the arrested illegal miners.",
		"There is political interference in the prosecution of both local and foreigner illegal miners. Whenever illegal miners were arrested, there are influential local people with political connections who get them release.",
		"There is an increase in demand for gold leading to an increase in mining activity.",
		"None of the above."
	]
)

scenario4 = Scenario(
	"In Ghana two systems of mining are in conflict: large-scale, regulated, corporate mining and widespread but small-scale, unregulated, illegal mining. The former has less of a negative impact on the environment but also provides less of a benefit to the surrounding communities than does the latter, which provides more jobs but wreaks havoc on the environment.  Imagine a third option: government-organized and regulated, community-scale operations conducted in partnership with corporate mines.  Government provides access to land and civil infrastructure; corporations, to equipment and world markets.  The main reason the third option will not succeed is because:",
	[
		"Government officials believe that any compromise makes them look weak so that they will not advance such an option.",
		"The government is unable to enforce mining protocols for a distributed collection of mines in the same way it can for a single, large mine so that environmental benefits are not realized.",
		"No role is provided for local chiefs, whose power will be diluted by this third possibility, making it unlikely that they come on board.",
		"Galamseyers have a strike it rich, gold rush attitude and will not exchange it for the certain, but barely sufficient payout from other options.",
		"None of the above."
	]
)

scenario5 = Scenario(
	"The destructive consequences of many mining operations are not directly related to the local disappearance of the mined material, but to the use of water in the extraction process.  Water is a shared resource that does not remain localized and must be used and reused multiple times by groups of people with competing needs and interests. In Ghana, deposits of minerals like bauxite are sometimes located in forested, mountainous areas which are sources of clean water for large populations.  The population will fear tainting of their water. Suggested construction of a bauxite mine in such a location is most likely to provoke what kind of reaction:",
	[
		"People will point out that proceeds from the sale of bauxite will more than pay for transport of clean water from elsewhere.",
		"Yet unrealized and even more valuable uses for clean water will be explored, such as supporting a natural habitat attractive to ecotourism, which is successful in other parts of the country.",
		"Farmers will point out that they cannot raise food in runoff from a bauxite mine and predict a consequent food shortage.",
		"A plan will be devised to transport the bauxite downstream for processing with water that has already been used for agricultural purposes.",
		"None of the above."
	]
)

number_of_paraphrases = 5

def paraphrase(sentence):

	text = "Paraphrase the following sentence " + sentence + " in " + str(number_of_paraphrases-1) + " unique ways. The answer must only have the paraphrases one per row."

	chat_completion = openai.ChatCompletion.create(model="gpt-4",
												   messages=[{"role": "user", "content": text}])

	result = chat_completion.choices[0].message.content

	result = result.split('\n')

	final_result = []

	for strr in result:
		index = 0
		for i in range(len(strr)):
			if ('a' <= strr[i] <= 'z') or ('A' <= strr[i] <= 'Z'):
				index = i
				break
		if len(strr[index:]) < 2:
			continue
		final_result.append(strr[index:])

	final_result.append(sentence)

	if len(final_result) != number_of_paraphrases:
		print("ERR paraphrases")
		return paraphrase(sentence)

	return final_result

# type = 0 -> only nr_paraphrases combinations
# type = 1 -> all possible combinations

def rank_choices(introduction, context, choices):

	question = "You are given the following question " + introduction + " and context about the situation through the " \
			   "following sentences: " + context + " \n\n " + " Use those sentences to rank the following from best to " \
			   "worst while ONLY using the information given above and be careful to cite each information used" \
															  ": \n" + "\n".join(choices)

	chat_completion = openai.ChatCompletion.create(model="gpt-4",
												   messages=[{"role": "user", "content": question}])

	result = chat_completion.choices[0].message.content

	#print("CHOICES:")
	#print(choices)
	#print()
	#print("EXPLANATION:")
	#print(result)

	ranks = []
	crr = 0

	for choice in choices:
		index = result.find(choice)
		ranks.append([index, crr])
		crr += 1

	temp = sorted(ranks)
	ranks = []

	for rank in temp:
		ranks.append(rank[1])

	for i in range(len(ranks)):
		ranks[i] = number_of_paraphrases - ranks[i] - 1

	outputs = []

	for i in range(1, len(ranks)+1):
		first_split = result.split(str(i) + ".")
		#print("I + " + str(i))
		#print("X + " + str(first_split))
		if len(first_split) >= 2:
			first_split = first_split[1]
		else:
			first_split = first_split[0]
		#print("Y + " + str(first_split))
		first_split = first_split.split(str(i+1) + '.')[0]
		#print("Z + " + str(first_split))
		outputs.append(first_split)

	#print(outputs)

	if len(outputs) != len(ranks):
		print("ERR Parsing")
		return rank_choices(introduction, context, choices)

	return ranks, outputs

def one_explanation(outputs):

	question = "You are given a sentence that is formulated and justified in several ways. Combine those justifications" \
			   "into a single one for all the sentences while citing the original justifications: \n\n" + '\n\n'.join(outputs)

	#print("XX " + question)

	chat_completion = openai.ChatCompletion.create(model="gpt-4",
												   messages=[{"role": "user", "content": question}])

	result = chat_completion.choices[0].message.content

	return result

	#print("YY " + result)


def compute_ranking(paraphrases, introduction, context):

	final_ranks = []

	for i in range(len(paraphrases)):
		final_ranks.append(0)

	all_outputs = []

	for i in range(number_of_paraphrases):
		print("STARTING " + str(i))
		choices_chosen = []
		for choice_list in paraphrases:
			choices_chosen.append(choice_list[i])
		ranks, outputs = rank_choices(introduction, context, choices_chosen)
		all_outputs.append(outputs)
		for i in range(len(ranks)):
			final_ranks[i] += ranks[i]

	justifications = []

	for i in range(len(final_ranks)):
		to_send = []
		for j in range(len(all_outputs)):
			to_send.append(all_outputs[j][i])
		#print("TT " + str(to_send))
		justifications.append(one_explanation(to_send))

	print(final_ranks)

	sum = 0

	for rank in final_ranks:
		sum += numpy.exp(rank)

	probabilities = []

	for rank in final_ranks:
		probabilities.append(numpy.exp(rank)/sum)

	print("The final probabilities for each choice are: " + str(probabilities))
	print("The per-choice explanations using the context are: ")
	print("")

	for i in range(len(ranks)):
		print("Choice: " + str(paraphrases[i][number_of_paraphrases-1]))
		print("Justification: " + str(justifications[i]))
		print("Justification secondary: " + str(all_outputs[number_of_paraphrases-1][i]))
		print("")

if __name__ == "__main__":

	# True if we first filter by the introduction and then choose by the choice
	filter_first = False

	# Choose if we want the sentences printed
	print_sentences = True

	# Use this if we combine the introduction and choice embeddings
	threshold = 0.4

	# Use this if we first filter by introduction and then by choice
	threshold1 = 0.3
	threshold2 = 0.6

	tokens_allowed = 12000

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

	scenario_chosen = scenario4

	scenario_match = matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed, False, False)

	print("CONTEXT: \n" + scenario_match)

	choices_str = ""

	for index in range(len(scenario_chosen.choices)):
		choices_str += str(index+1) + ") " + scenario_chosen.choices[index] + "\n\n"

	#final_sentence = "You have to look carefully at the following sentences and then rank several choices based on which" \
#					 "ones are most likely to be true. The sentences are: \n\n" + scenario_match + "\n\n Now rank the " \
#					 "following choices based on their likelyhood while also giving intuition behind the choices from" \
#					 "the context given above:\n\n" + choices_str

	#print(scenario_match)

	openai.api_key_path = "openai_key"

	paraphrases = []

	for choice in scenario_chosen.choices:
		result = paraphrase(choice)
		paraphrases.append(result)
		if len(result) != number_of_paraphrases:
			print("ERROR we don't have " + str(number_of_paraphrases) + "paraphrases")

	choices_chosen = []

	for paraphrase in paraphrases:
		choices_chosen.append(paraphrase[0])

	compute_ranking(paraphrases, scenario_chosen.introduction, scenario_match)


	#scenario_match = matcher.match_scenario(scenario2)
	#print(scenario_match)
	#scenario_match = matcher.match_scenario(scenario3)
	#print(scenario_match)
