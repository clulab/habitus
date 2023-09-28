from pathlib import Path

class MockChat:
	def __init__(self, filenames: list[str]):
		self.filenames = filenames
		self.current_prompt = 0
		self.question = "" # accessed externally

	def _get_output(self) -> str:
		filename = self.filenames[self.current_prompt % len(self.filenames)]
		output = Path(filename).read_text(encoding="utf-8")
		self.current_prompt += 1
		return output

	def calL_gpt(self) -> str:
		output = self._get_output()
		return output

	def calL_gpt_double(self, last_answer: str, last_question: str) -> str:
		output = self._get_output()
		return output
	