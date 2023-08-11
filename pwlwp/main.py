
from argparse import ArgumentParser
from matcher import Matcher
from scenario import Scenario
from sentence_transformers import SentenceTransformer
from typing import Tuple

import pandas

def get_in_and_out() -> Tuple[str] # , str]:
	argument_parser = ArgumentParser()
	argument_parser.add_argument("-i", "--input", required=True, help="input directory name")
	# argument_parser.add_argument("-o", "--output", required=True, help="output file name")
	args = argument_parser.parse_args()
	return args.input #, args.output

scenario1 = Scenario(
	"In the current Ghanaian market, a pound of gold is worth $30,000.  However, an illegal gold miner sells it for about $420. Imagine that gold is discovered in a neighboring country in even greater quantities, shifting the mining industry and causing prices to plummet in Ghana. Now, illegal miners are only receiving $200 for a pound of gold. Illegal mining has started to decline across the country. This is likely because…",
	[
		"The payoff of illegal gold mining no longer justifies the risk.",
		"Other jobs in Ghana now offer higher pay, causing those involved in illegal gold mining to seek opportunities elsewhere.",
		"Those involved in illegal gold mining are now moving abroad, where the value of gold is still higher.",
		"Gold has lost its value overall, causing Ghanaians to lose interest in the industry."
	]
)

scenario2 = Scenario(
	"Imagine that illegal mining has become so bad in Ghana that it has become nearly impossible for vegetation to survive in the soil. This is caused by the destruction of the soil’s original nature profile as different ground layers have been mixed up over time. There is now a food shortage developing as a result of locals being unable to harvest their own food. However, illegal mining of gold continues and those struggling the most with the food crisis are also most active in the illegal mining industry. This is likely because…",
	[
		"There's a lack of education around the correlation between illegal mining and the destruction of the soil.",
		"The illegal miners find more value in the money gained through illegal mining than the vegetation they can grow.",
		"There is an assumption that the problem is temporary, and the government will step in.",
		"There were problems with the soil and food shortages prior to the illegal mining industry consuming the country, so many people are numb to the difficulties."
	]
)

scenario3 = Scenario(
	"Imagine the government makes a renewed effort to arrest illegal miners and seize and burn their equipment. However, illegal mining does not decrease, this is because:",
	[
		"There is no alternative, so miners are willing to take the risk.",
		"The security forces are taking bribes to release the arrested illegal miners.",
		"There is political interference in the prosecution of both local and foreigner illegal miners. Whenever illegal miners were arrested, there are influential local people with political connections who get them release.",
		"There is an increase in demand for gold leading to an increase in mining activity."
	]
)


if __name__ == "__main__":
	threshold = 0.3
	sentence_transformer_name: str = "all-distilroberta-v1"
	input_file_name: str = "../corpora/causalBeliefSentences.tsv"
	# input_file_name, output_file_name = get_in_and_out()
	data_frame = pandas.read_csv(input_file_name, sep="\t", encoding="utf-8")
	sentence_transformer = SentenceTransformer(sentence_transformer_name)
	matcher = Matcher(sentence_transformer, data_frame, threshold)
	scenario_match = matcher.match_scenario(scenario1)
	print(scenario_match)
