import openai
import os

class Chat:

	question = ""
	model = "gpt-4"
	timestr = ""
	current_prompt = 0

	def __new__(cls, *args, **kwargs):
		return super().__new__(cls)

	def __init__(self, model, question, timestr):
		openai.api_key_path = "openai_key"
		self.question = question
		self.model = model
		self.timestr = timestr
		os.mkdir("gpt_chat/" + timestr)

	def calL_gpt(self) -> str:

		chat_completion = openai.ChatCompletion.create(model=self.model,
													   temperature=0,
													   messages=[{"role": "user", "content": self.question}])

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_input.txt", "w")
		f.write(self.question)
		f.close()

		output = chat_completion.choices[0].message.content

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_output.txt", "w")
		f.write(output)
		f.close()

		self.current_prompt += 1

		return output

	def calL_gpt_double(self, last_answer: str, last_question: str) -> str:

		chat_completion = openai.ChatCompletion.create(model=self.model,
													   temperature=0,
													   messages=[{"role": "user", "content": last_question},
																 {"role": "assistant", "content": last_answer},
																 {"role": "user", "content": self.question}])

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_input.txt", "w")
		f.write(self.question)
		f.close()

		output = chat_completion.choices[0].message.content

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_output.txt", "w")
		f.write(output)
		f.close()

		self.current_prompt += 1

		return output