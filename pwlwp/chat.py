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

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_input.txt", "w", encoding='utf-8')
		f.write(self.question)
		f.close()

		# PLACEHOLDER FOR TESTING
		output = """```The Chinese mining project pays less and does not offer additional benefits to locals```
This choice can be justified by the context that suggests that Chinese companies often exploit local workers and do not fulfill their corporate social responsibility. The context states, "However, issues of artisanal miners being exploited at the mining site, corporate social responsibility and non-payment of locals continue to cause commotion in the mining sites." This suggests that the Chinese mining project may not offer competitive pay or additional benefits to the local artisanal miners, making them reluctant to join the project. Furthermore, the context also mentions that "But even as exploration or mining goes on, the communities have not seen the promised benefits; nor have the activities been based on any consensual agreement between the communities and the companies over the use of their land." This further supports the idea that the Chinese mining project may not be offering sufficient benefits to the locals.

```Locals prefer to work for themselves```
This choice can be justified by the context that suggests that artisanal mining is not just a business for the local communities, but a way of life. The context states, "First of all, we need to appreciate as a country that small-scale mining in most of these communities is not just a business but a way of life." This suggests that the locals may prefer to continue their traditional way of life, which includes artisanal mining, rather than joining a large-scale mining project. Furthermore, the context also mentions that "Artisanal miners feel they have a stake and so do the other parties," which suggests that the locals may prefer to work for themselves where they have a stake and control over their work.

```Locals prefer to have the option of farming and artisanal mining than working exclusively for a Chinese mining project```
This choice can be justified by the context that suggests that the locals may prefer to have the flexibility to engage in both farming and artisanal mining, rather than being tied down to a single large-scale mining project. The context states, "And after mining, they will re-arrange the place, prepare it for agricultural development and shift people to resettle in their areas." This suggests that the locals may value the ability to engage in both farming and mining, which may not be possible if they were to join the Chinese mining project.

```Locals do not trust Chinese```
This choice can be justified by the context that suggests that there is a lack of trust between the local communities and the Chinese companies. The context states, "Many miners feel this is another ploy by government to disenfranchise small players in favour of established investors." This suggests that the locals may view the Chinese mining project as a ploy to disenfranchise them, leading to a lack of trust. Furthermore, the context also mentions that "Some of the activities of Chinese migrants, mainly in the artisanal and retail trade sectors, have generated resentment as they compete directly with locals." This further supports the idea that there may be a lack of trust between the locals and the Chinese.

```None of the above```
This choice cannot be justified by the context as the context provides evidence to support each of the other choices."""

		output = chat_completion.choices[0].message.content

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_output.txt", "w", encoding='utf-8')
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

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_input.txt", "w", encoding='utf-8')
		f.write(self.question)
		f.close()

		# PLACEHOLDER FOR TESTING
		output = '''[
  {
    "id": 1,
    "choice": "Locals prefer to work for themselves",
    "rank": 1,
    "justification": "The context suggests that artisanal mining is not just a business but a way of life for the local communities. This is supported by the statement, 'First of all, we need to appreciate as a country that small-scale mining in most of these communities is not just a business but a way of life.' Additionally, the locals have been involved in artisanal mining before the Chinese firm arrived, as indicated by 'The artisanal miners argue that the Chinese firm found them when they were already prospecting for mineral activities in the same area.' This suggests a preference for self-employment and independence in their work."
  },
  {
    "id": 2,
    "choice": "Locals prefer to have the option of farming and artisanal mining than working exclusively for a Chinese mining project",
    "rank": 2,
    "justification": "The context suggests that the locals value the flexibility and diversity of their livelihoods, which include both farming and artisanal mining. This is supported by the statement, 'And after mining, they will re-arrange the place, prepare it for agricultural development and shift people to resettle in their areas.' This suggests that the locals value the ability to engage in both farming and mining, rather than being tied to a single mining project."
  },
  {
    "id": 3,
    "choice": "The Chinese mining project pays less and does not offer additional benefits to locals",
    "rank": 3,
    "justification": "The context suggests that there are issues of exploitation and non-payment at the mining sites, as indicated by 'However, issues of artisan miners being exploited at the mining site, corporate social responsibility and non-payment of locals continue to cause commotion in the mining sites.' However, it is not explicitly stated that the Chinese mining project pays less or does not offer additional benefits to locals."
  },
  {
    "id": 4,
    "choice": "Locals do not trust Chinese",
    "rank": 4,
    "justification": "The context suggests some level of mistrust or resentment towards Chinese companies, as indicated by 'Some of the activities of Chinese migrants, mainly in the artisanal and retail trade sectors, have generated resentment as they compete directly with locals.' However, it is not explicitly stated that the locals do not trust the Chinese in the context of the mining project."
  },
  {
    "id": 5,
    "choice": "None of the above",
    "rank": 5,
    "justification": "The context provides evidence to support the other choices to varying degrees, making 'None of the above' the least likely choice."
  }
]'''

		output = chat_completion.choices[0].message.content

		f = open("gpt_chat/" + self.timestr + "/" + str(self.current_prompt) + "_output.txt", "w", encoding='utf-8')
		f.write(output)
		f.close()

		self.current_prompt += 1

		return output