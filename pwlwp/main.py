
from argparse import ArgumentParser

from chat import Chat
from matcher import Matcher
from mock_chat import MockChat
from scenario import Scenario
from sentence_transformers import SentenceTransformer

import json
import numpy
import pandas

import os

import time

def get_in_and_out() -> str: # Tuple[str, str]:
	argument_parser = ArgumentParser()
	argument_parser.add_argument("-ic", "--input-corpus", required=True, help="input corpus file name")
	argument_parser.add_argument("-iv", "--input-vector", required=True, help="input vector file name")
	args = argument_parser.parse_args()
	return args.input_corpus, args.input_vector

chat_gpt = Chat("gpt-4", "", time.strftime("%Y%m%d-%H%M%S"))
# chat_gpt = MockChat([f"./pwlwp/gpt_chat/20230923-021014/{index}_output.txt" for index in range(0, 25)])

scenario_official_1a = Scenario(
	"Imagine the Ghanaian government implements reforms that change the time it takes for local residents to obtain a legal mining license, reducing the time from three years to three months. Suppose that within three months, the number of mining license applications received by the government tripled. This would have been most likely because…",
	[
		"Ghanaians that were participating in illegal mining felt comfortable applying for a license because of the shortened wait time",
		"Ghanaians who were not previously mining now see it as a new economic opportunity",
		"Ghanaians are influenced to seek mining licenses by their friends/family",
		"None of the above"
	]
)

scenario_official_1b = Scenario(
	"Imagine the Ghanaian government implements reforms that change the time it takes for local residents to obtain a mining license – traditionally a big motivation for illegal mining. Instead of three years, they can now do it in three months. Suppose that a month after implementing the new reform, the number of mining licenses granted has remained the same. This would have been likely because…",
	[
		"Ghanaians involved in illegal mining do not trust the government and would prefer to operate without a license",
		"Ghanaians who would like a license are unaware of the new, expedited licensing process",
		"Ghanaians think three months is too long to obtain a mining license",
		"Ghanaians involved in illegal mining do not want to risk being taxed by authorities, while Ghanaians that are not involved in illegal mining are not interested in legal mining",
		"None of the above"
	]
)
scenario_official_2a = Scenario(
	"Imagine that China has recently completed a mining infrastructure project in Brong Ahafo, Ghana. Galamseyers in the region have received an offer to work in the new mine. Additionally, imagine that the Chinese government has promoted the new large-scale mining effort as a source of legal employment for locals engaged in small-scale illegal mining. Suppose that farmers continue to engage in illegal mining at the same rate. This is because…",
	[
		"The Chinese galamseyers have taken all available jobs",
		"The Chinese mining project is less lucrative than illegal mining",
		"Ghanaians do not want to work for the Chinese",
		"Ghanaians would prefer to have the option of farming and illegal mining than exclusively work for a Chinese mining company",
		"None of the above"
	]
)

scenario_official_2b = Scenario(
	"Imagine that China has recently completed a mining infrastructure project in Brong Ahafo, Ghana. Chinese galamseyers in the region have received an offer to work in the new mine. Additionally, imagine that the Chinese government has promoted the new large-scale mining effort as a source of employment for locals engaged in small-scale illegal mining. Suppose that within a month, the Ghanaian government has seen a decrease in illegal mining activities in Brong Ahafo. This is because…",
	[
		"Those involved in illegal mining have begun working for the Chinese mining company",
		"Those involved in illegal mining have sought other opportunities outside of the region because of the Chinese mine opening",
		"Those involved in illegal mining have sought opportunities in small-scale agriculture because of the Chinese mine opening",
		"Those involved in illegal mining have sought other opportunities within the region unrelated to agriculture",
		"None of the above"
	]
)

scenario_official_3 = Scenario(
	"An area typically dedicated to small-scale agriculture has been overcome by illegal mining, destroying the land and taking up labor that would otherwise be involved in agriculture. This has resulted in mass protests and tensions. What impact would this have on illegal mining activities?",
	[
		"Those involved in illegal mining would listen to their friends and stop",
		"Those involved in illegal mining would continue in secret",
		"Those involved in illegal mining would not care about the opinions of their close friends or family",
		"Those involved in illegal mining would relocate to a different region, where they would continue mining",
		"None of the above"
	]
)

scenario_official_4a = Scenario(
	"Imagine there has been a U.S.-led effort to promote the agricultural sector in southern Ghana. Much of this effort is tied to sustainability and climate change. The U.S. committed $10 million to this new effort, boosting Ghanaian agribusiness and prospective job opportunities. Suppose that local governments then notice that farming activities recently doubled. The most likely reason for this would have been because…",
	[
		"Those previously involved in illegal mining have started to move back to farming",
		"Those who were never engaged in mining or farming have decided to take advantage of the financial opportunity",
		"Traditional farmers have increased output because of U.S.-led financing",
		"Farmers from neighboring regions have immigrated to southern Ghana",
		"None of the above"
	]
)

scenario_official_4b = Scenario(
	"Imagine there has been a U.S.-led effort to promote the agricultural sector in southern Ghana. Much of this effort is tied to sustainability and climate change. The U.S. committed $10 million to this new effort in hopes of boosting Ghanaian agribusiness and prospective job opportunities. Local governments notice, however, that farming activities have remained unchanged. The most likely reason for this would have been because…",
	[
		"Those working in illegal mining are skeptical of U.S. investments",
		"Those working in illegal mining think they’re already making a good livelihood",
		"Local authorities are diverting the U.S. investment away from agribusiness",
		"The investment has attracted migrants from other regions who are not interested in farming but hope to find other economic opportunities",
		"None of the above"
	]
)

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

scenario6 = Scenario(
	"In the current Ghanaian market, a pound of gold is worth $30,000.  However, an illegal gold miner sells it for about $420. Imagine that gold is discovered in a neighboring country in even greater quantities, shifting the mining industry and causing prices to plummet in Ghana. Now, illegal miners are only receiving $200 for a pound of gold. Illegal mining has started to decline across the country. This is likely because…",
	[
		"The payoff of illegal gold mining no longer justifies the risk.",
		"This choice is not right.",
		"Those involved in illegal gold mining are now moving abroad, where the value of gold is still higher.",
		"This is the correct choice.",
		"None of the above."
	]
)


scenario_legal_1a = Scenario(
	"Imagine that the Ghanaian government implements a reform to put cash into the hands of Ghanaian youth involved in illegal small-scale mining or galamsey through alternative economic opportunities in an effort to provide livelihoods in areas where galamsey proliferates.  Suppose that within three months after the launch of the reform, illegal small-scale mining or galamsey in those areas has been reduced by half.  The most likely explanation for the success of the reform is because….",
	[
	]
)

scenario_legal_1b = Scenario(
	"Imagine that the Ghanaian government implements a reform to put cash into the hands of Ghanaian youth involved in illegal small-scale mining or galamsey through alternative economic opportunities in an effort to provide livelihoods in areas where galamsey proliferates.  Suppose that within three months after the launch of the reform, illegal small-scale mining or galamsey in those areas has not changed.  The most likely explanation for the failure of the reform is because…",
	[
	]
)
scenario_legal_2a = Scenario(
	"Imagine that the Ghanaian government used soldiers and police personnel to curb illegal small-scale mining or galamsey.  Military-style operations punished illegal small-scale mining or galamsey activity. They arrested many illegal miners or galamseyers and seized mining equipment. Suppose that within three months after the launch of the campaign, illegal small-scale mining or galamsey had decreased. The most likely explanation for the success of the operations is because…",
	[
	]
)

scenario_legal_2b = Scenario(
	"Imagine that  the Ghanaian government used soldiers and police personnel to curb illegal small-scale mining or galamsey.  Military-style operations punished illegal small-scale mining or galamsey activity.  They arrested many illegal miners or galamseyers and seized mining equipment.  Suppose that within three months after the launch of the campaign, illegal small-scale mining or galamsey had not changed or in some areas had even increased.  The most likely explanation for the failure of the operation is because…",
	[
	]
)

scenario_legal_3a = Scenario(
	"Imagine that the Ghanaian government attempted to formalize illegal small-scale mining or galamsey in Ashanti by introducing a new Minerals and Mining Act which has a provision for licenses for small-scale mining.  Suppose that within three months after the launch of the law,  illegal small-scale mining or galamsey has decreased.  Many illegal small-scale miners or galamseyers applied for small-scale mining licenses.  The most likely explanation for the success of the law is because…",
	[
	]
)

scenario_legal_3b = Scenario(
	"Imagine that the Ghanaian government attempted to formalize illegal small-scale mining or galamsey in Ashanti by introducing a new Minerals and Mining Act which has a provision for licenses for small-scale mining.  Suppose that within three months after the launch of the law,  illegal small-scale mining or galamsey has not decreased.  The most likely explanation for the failure of the law is because…",
	[
	]
)

scenario_uganda_1a = Scenario(
    "Imagine that the new Mining Act of 2022 has motivated local Karamoja people to obtain an artisanal mining license rather than operating illegally. Suppose that within three months the number of license applications received by the government increased three times. This is because...",
    [
        "Locals who were participating in artisanal mining without a license felt motivated to apply for a license",
        "Locals who were not previously mining now see it as a new economic opportunity",
        "Local authorities setup a system to help locals apply for an artisanal mining license",
        "Locals are influenced by the arrival of non-Karamoja people who came to mine Karamoja minerals",
        "None of the above"
    ]
)

scenario_uganda_1b = Scenario(
    "Imagine that the new Mining Act of 2022 has motivated local Karamoja people to obtain an artisanal mining license rather than operating illegally. Suppose that within three months the government did not receive any license applications. This is because...",
    [
        "Locals involved in illegal artisanal mining do not trust the government and would prefer to operate without a license",
        "Locals who would like a license are unaware of the new Mining Act of 2022 which allows applying for an artisanal mining license",
        "Locals who would like a license do not have financial means to pay license fees",
        "Locals involved in illegal artisanal mining are unaware that it is illegal",
        "None of the above"
    ]
)

scenario_uganda_2a = Scenario(
    "Imagine that China has recently opened a large-scale mining project in your area. Local artisanal miners have received an offer to work at this site. Suppose that local people continue to engage in artisanal mining without joining the Chinese mining project. This is because...",
    [
        "The Chinese mining project pays less and does not offer additional benefits to locals",
        "Locals prefer to work for themselves",
        "Locals prefer to have the option of farming and artisanal mining than working exclusively for a Chinese mining project",
        "Locals do not trust Chinese",
        "None of the above"
    ]
)

scenario_uganda_2b = Scenario(
    "Imagine that China has recently opened a large-scale mining project in your area. Local artisanal miners have received an offer to work at this site. Suppose that within a month the number of artisanal miners has decreased in the area. This is because...",
    [
        "Those involved in artisanal mining have begun working for the Chinese mining project",
        "Those involved in artisanal mining were forced to leave the area because the Chinese took control of the mining area where they used to mine",
        "Those involved in artisanal mining have sought other economic opportunities within the area",
        "Those involved in artisanal mining went back to farming (growing crops or rearing livestock)",
        "None of the above"
    ]
)

scenario_uganda_3a = Scenario(
    "Imagine there has been a U.S.-funded livelihoods project in Karamoja. The project aims to provide inputs and other supplies to increase crop and livestock production and create jobs for youth in Karamoja. Suppose that local government then notice that farming and livestock activities recently increased. This is because...",
    [
        "Those previously involved in artisanal mining have started to move back to farming and livestock",
        "Those who were never engaged in mining decided to take advantage of opportunities offered by the project",
        "Farmers from neighboring districts to Karamoja have moved to the area to participate in the project",
        "None of the above"
    ]
)

scenario_uganda_3b = Scenario(
    "Imagine there has been a U.S.-funded livelihoods project in Karamoja. The project aims to provide inputs and other supplies to increase crop and livestock production and create jobs for youth in Karamoja. Suppose that local government then notice that farming or livestock activities have remained unchanged. This is because...",
    [
        "Those working in artisanal mining are skeptical of the U.S.-funded project",
        "Those working in artisanal mining think they are already making a good livelihood and do not wish to change it",
        "Those working in artisanal mining can both mine and participate in the U.S.-funded project",
        "None of the above"
    ]
)

scenario_uganda_4 = Scenario(
    "An area which was typically allocated by LC1 for farming and livestock rearing has been taken away for mining, destroying the land and vegetation around it. This has resulted in community tensions with the mining operator. What impact would this have on mining activities?",
    [
        "Those involved in mining would continue mining",
        "Those involved in mining would relocate to a different area where they would continue mining",
        "Security (army) was moved to the area to maintain peace",
        "Mining operator would offer benefits to the community to allow them to mine the area",
        "None of the above"
    ]
)

number_of_paraphrases = 10

def paraphrase(sentence: str, context: str) -> list[str]:

	text = "Paraphrase the following sentence ```" + sentence + "``` in " + str(number_of_paraphrases-1) + " unique ways based on the context ```" + context + "```. Only change word ordering, do not change the meaning at all. The answer must only have the paraphrases one per row."

	chat_gpt.question = text
	result = chat_gpt.calL_gpt()

	result = result.split('\n')

	final_result = []

	for string_temp in result:
		index = 0
		for i in range(len(string_temp)):
			if ('a' <= string_temp[i] <= 'z') or ('A' <= string_temp[i] <= 'Z'):
				index = i
				break
		if len(string_temp[index:]) < 2:
			continue
		final_result.append(string_temp[index:])

	final_result.append(sentence)

	if len(final_result) != number_of_paraphrases:
		print("ERR paraphrases")
		return paraphrase(sentence)

	return final_result

# type = 0 -> only number_paraphrases combinations
# type = 1 -> all possible combinations

def rank_choices(introduction: str, context: str, choices: list[str]) -> (list[tuple], list[str]):

	#question = "You are given the following question " + introduction + " and context about the situation through the " \
	#		   "following sentences: " + context + " \n\n " + " Use those sentences to rank the following from best to " \
	#		   "worst while ONLY using the information given above and be careful to cite each information used" \
	#														  ": \n" + "\n".join(choices)

	choices_str = "\n".join(choices)

	last_question \
= f"""Read the following question delimited with backticks ``` {introduction} ```. Use the following context sentences delimited with backticks as background knowledge ``` {context} ```.
      Provide long and thorough justifications for each of the choices independently, without referring to the other choices, while citing the context using quotes:
	```{choices_str}```"""

	chat_gpt.question = last_question
	last_answer = chat_gpt.calL_gpt()

	#print("@@@ " + str(last_answer))

	question \
= f"""Rank each choice from most likely to be true to least likely and copy the justification as JSON format with the fields: (id, choice, rank, justification)"""

	#print("U " + question)

	chat_gpt.question = question
	result = chat_gpt.calL_gpt_double(last_answer, last_question)

	#print("!!! " + str(result))

	#ranks = []
	#current = 0

	#for choice in choices:
	#	index = result.find(choice)
	#	ranks.append([index, current, current])
	#	current += 1

	#temp = sorted(ranks)
	#ranks = []

	#for rank in temp:
	#	ranks.append(rank)

	#outputs = ['' for _ in range(len(ranks))]

	#for i in range(1, len(ranks)+1):
	#	first_split = result.split(str(i) + ".")
	#	if len(first_split) >= 2:
	#		first_split = first_split[1]
	#	else:
	#		first_split = first_split[0]
	#	first_split = first_split.split(str(i+1) + '.')[0]
	#	outputs[ranks[i-1][2]] = first_split

	#if len(outputs) != len(ranks):
	#	print("ERR Parsing")
	#	return rank_choices(introduction, context, choices)

	return json.loads(result)

def one_explanation(outputs: list[str]) -> str:

	question = "You are given a sentence that is formulated and justified in several ways. Combine those justifications" \
			   "into a single one for all the sentences while citing the original justifications: \n\n" + '\n\n'.join(outputs)

	chat_gpt.question = question
	result = chat_gpt.calL_gpt()

	return result



def ask_gpt(to_ask: str) -> str:

	question = to_ask

	chat_gpt.question = question
	result = chat_gpt.calL_gpt()

	return result

def compute_ranking(temporary_choices: list[list[str]], introduction: str, context: str, gamma: float, scenario: Scenario, f):

	final_rankings = {}

	for choice in temporary_choices[0]:
		final_rankings[choice] = 0

	all_outputs = []

	for i in range(len(temporary_choices)):
		print("STARTING " + str(i))
		#choices_chosen = [choice_list[i] for choice_list in paraphrases]
		json = rank_choices(introduction, context, temporary_choices[i])
		all_outputs.append(json)
		for item in json:
			final_rankings[item["choice"]] += len(temporary_choices[0]) - int(item["rank"])
			#final_ranks[int(item["initial_id"])-1] += number_of_paraphrases - int(item["rank"]) - 1

	f.write(str(final_rankings) + "\n")

	sum = 0

	for rank in final_rankings.values():
		sum += numpy.exp(rank*gamma)

	probabilities = [0 for _ in range(len(scenario.choices))]

	for i in range(len(scenario.choices)):
		probabilities[i] = numpy.exp(final_rankings[scenario.choices[i]]*gamma) / sum

	#probabilities = [(numpy.exp(rank*gamma)) / sum for rank in final_ranks]

	f.write("The final probabilities for each choice are: " + str(probabilities) + "\n")

	for i in range(len(probabilities)):
		f.write("Probability for choice " + str(chr(ord('A') + i)) + " is " + str(round(probabilities[i]*100, 2)) + "%." + "\n")

	f.write("The per-choice explanations using the context are: " + "\n")
	f.write("")

	for i in range(len(scenario.choices)):
		f.write("Choice " + str(chr(ord('A') + i)) + " (" + str(scenario.choices[i]) + "):" + "\n")
		correct_justification = ""
		for item in json:
			if item["choice"] == scenario.choices[i]:
				correct_justification = item["justification"]
		f.write("Justification: " + str(correct_justification) + "\n")
		f.write("\n")

def get_scenario_match(i: int):
	if i == 0:
		return matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed, False, False)
	if i == 1:
		return matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed, True, False)
	if i == 2:
		return matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed, False, True)
	if i == 3:
		return ""
	if i == 4:
		return matcher.match_scenario(scenario_chosen, print_sentences, filter_first, tokens_allowed, True, True)

if __name__ == "__main__":

	print()

	# True if we first filter by the introduction and then choose by the choice
	filter_first = False

	# Choose if we want the sentences printed
	print_sentences = True

	# Use this if we combine the introduction and choice embeddings
	threshold = 0.4

	# Use this if we first filter by introduction and then by choice
	threshold1 = 0.3
	threshold2 = 0.6

	# Control softmax spiking
	gamma = 0.2

	tokens_allowed = 12000

	if filter_first:
		threshold = threshold1

	sentence_transformer_name: str = "all-MiniLM-L6-v2"
	input_corpus_file_name: str = "../corpora/uganda.tsv"
	input_vector_file_name: str = "../corpora/uganda.npy"
	# input_corpus_file_name, input_vector_file_name = get_in_and_out()
	input_vectors = numpy.load(input_vector_file_name)
	data_frame = pandas.read_csv(input_corpus_file_name, sep="\t", encoding="utf-8", keep_default_na=False,
		dtype={"file": str, "index": int, "sentence": str, "causal": bool, "belief": bool}
	) # [:100]
	print(len(data_frame))
	sentence_transformer = SentenceTransformer(sentence_transformer_name)

	matcher = Matcher(sentence_transformer, input_vectors, data_frame, threshold, threshold2)

	#scenarios_list = [(scenario_legal_1a, "scenario_legal_1a"),
#					  (scenario_legal_1b, "scenario_legal_1b"),
#					  (scenario_legal_2a, "scenario_legal_2a"),
#					  (scenario_legal_2b, "scenario_legal_2b"),
#					  (scenario_legal_3a, "scenario_legal_3a"),
#					  (scenario_legal_3b, "scenario_legal_3b")]

	#scenarios_list = [(scenario_official_1a, "scenario_official_1a")]

	scenarios_list = [
		#(scenario_uganda_1a, "scenario_uganda_1a"),
		#(scenario_uganda_1b, "scenario_uganda_1b"),
		(scenario_uganda_2a, "scenario_uganda_2a"),
		(scenario_uganda_2b, "scenario_uganda_2b"),
		(scenario_uganda_3a, "scenario_uganda_3a"),
		(scenario_uganda_3b, "scenario_uganda_3b"),
		(scenario_uganda_4, "scenario_uganda_4")
	]

	for scenario in scenarios_list:

	#scenario_chosen = scenario_official_2b

		print(scenario[1] + ":")

		scenario_chosen = scenario[0]

		print("Prompt:", scenario_chosen.introduction)
		print("Choices:")

		for choice in scenario_chosen.choices:
			print(choice)

		#print("Choices:", scenario_chosen.choices)

		print()

		#continue


		path = "results/" + scenario[1]

		os.mkdir(path)

		scenario_chosen = scenario[0]

		for context_index in range(5):

			current_type = "all"

			if context_index == 1:
				current_type = "beliefs"
			if context_index == 2:
				current_type = "causal"
			if context_index == 3:
				current_type = "no_context"
			if context_index == 4:
				current_type = "beliefpluscausal"

			f = open(path + "/" + current_type + "_question.txt", "w", encoding='utf-8')

			combinations = []
			temporary_choices = []

			for i in range(len(scenario_chosen.choices)):
				temporary_choices.append(numpy.roll(scenario_chosen.choices, i))

			scenario_match = get_scenario_match(context_index)

			# USE THIS FOR QUICK ANSWERING - SAME AS CHAT
			#target = f"Use the following context sentences delimited with backticks as background knowledge ``` {scenario_match} ``` to explain the most likely answer to the question ``` {scenario_chosen.introduction} ```. Provide long and thorough justifications, while citing the context using quotes."
			#f.write(target)
			#f.close()
			#answer_gpt = ask_gpt(target)
			#f = open(path + "/" + current_type + "_answer.txt", "w")
			#f.write(answer_gpt)
			#f.close()

			#continue

			choices_str = ""

			for index in range(len(scenario_chosen.choices)):
				choices_str += str(index+1) + ") " + scenario_chosen.choices[index] + "\n\n"

			compute_ranking(temporary_choices, scenario_chosen.introduction, scenario_match, gamma, scenario_chosen, f)

			f.write("CONTEXT:" + "\n")

			f.write("\n")

			f.write(scenario_match + "\n")

			f.close()

