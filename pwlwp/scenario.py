
from typing import List

class Scenario():

	def __init__(self, introduction: str, choices: List[str]) -> None:
		super().__init__()
		self.introduction = introduction
		self.choices = choices

