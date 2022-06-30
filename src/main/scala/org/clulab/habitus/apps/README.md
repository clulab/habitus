                                        Here's what the Apps in this directory do:

* **AcronymApp** - Take an input directory of unstructured `.txt` files and print to the console the strings that might be acronyms.


* **ConditionalCaseApp** - Take a single input `.txt` file of unstructured text and split it into `.txt.preserved` and `.txt.restored` which contain either the same text or the text after case restoration.  In the process, the unstructured text is reformatted to have one sentence per line.  The output is not in tokenized format.  An attempt is made to preserve the whitespace.  The case restoration is more than what `processors` alone provides.  There is a `ConditionalHabitusProcessor` with a cutoff value that determines whether the `processors` version is used or not.


* **ConditionalCaseMultipleFileApp** - Do the same thing as the `ConditionalCaseApp` except on an entire directory of `.txt` files.


* **EvaluateCaseApp** - From a single unstructured text file specified as a parameter, create a report with extension `.evaluated.tsv` detailing how many of the words in the text file have different kinds of capitalization conventions after they have been passed through `processors` both with and without case restoration.  The output is used to figure out the appropriate cutoff value for the `ConditionalCaseApps`.


* **ExportNamedEntitiesApp** - Read a specific `.tsv` file that contains example sentences containing named entities that have in the past been misidentified and output a file in `BIO` notation that has turned the named entities into `O` entries.  This will be used to help the entities be unlearned.  The input `.tsv` file is not assumed to have been tokenized or corrected for case.


* **RestoreCaseApp** - This is a simpler version of the `ConditionalCaseApp` which always restores the case rather than doing it only conditionally.  It takes a single input filename specifying an unstructured text file and splits it into the `.preserved` and `.restored` versions.  An attempt is made to preserve whitespace, so output is not tokenized.


* **TrainGloveApp** - From an input directory of unstructured `.txt` files extract the entire text from each, tokenize it, and output one document per line to a specified output file.  This output format is what the glove training program expects.  See the `dockerGlove` directory for related files.
