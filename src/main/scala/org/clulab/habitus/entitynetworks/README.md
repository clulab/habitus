Here's what the Apps in this directory do:

* **LexiconNer** - Take an single input file of the sort output by ConditionalCaseMultipleFileApp and output a file with extension `.out` that lists all the words and their entities.  This is so that the entities can be checked.


* **LexiconNerMultipleFile** - Do the same thing as the above except on an entire directory of files with extension `.txt.restored`.  The pipeline for these files starting with the PDFs to retraining the NER is such:

    1. Fetch the PDFs from Google Drive.  There are 100 and they should already have been translated to English.
    1. Convert them to text using the [pdf2txt project](https://github.com/clulab/pdf2txt).  Along with the input and output directories, specify `-converter scienceparse -case false`.  If you use the `jar` file, please note the version number used: it is displayed in response to the `--help` argument.  If you use `sbt`, note the `git` tag or commit hash so that the conversion can be repeated.
    1. Run the `ConditionalCaseMutlipleFileApp` which will correct the case and output a file with extension `.txt.restored` with one tokenized sentence per line.
    1. Use the `App` documented here, `LexiconNerMultipleFile`, to extract the entities.
    1. Analyze the results, producing a list of false positive named entities and a spreadsheet with their sentences.
    1. Run the `ExportNamedEntitiesApp` to produce a file that can be used to retrain the named entity recognizer, which is part of the [processors project](https://github.com/clulab/processors).
