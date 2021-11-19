# HEURISTICS

This repository contains CLU lab's NLP software for the DARPA HEURISTICS project, which is part of the [HABITUS program](https://www.darpa.mil/program/habitus).

## 1. Requirements

This software requires:
- Java 8 
- sbt 1.x

## 2. Variable reading

This component reads for values assigned to variables that are important for crop modeling such as planting date and fertilizer usage. For example, from the sentence *"Sowing between October 4 and October 14 was optimal."* our software extracts the variable *"sowing"* with the value being the date range *"between October 4 and October 14"*, which is normalized to *"XXXX-10-04 -- XXXX-10-14"*. 

This component can be used in three different ways, as described below.

### 2.1. Read-eval-print loop (REPL)

We provide an interactive REPL interface, in which users can type natural language and inspect the structured output produced by the variable reader code. To call it, type:

```
./var-shell
```

For example, typing the sentence *"Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS."* (without the quotes), produces the following output:

```
...
events:
List(Assignment, Event) => sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March
	------------------------------
	Rule => Assignment-range-1
	Type => EventMention
	------------------------------
	trigger => ranged
	variable (Variable, Entity) => sowing dates
	value (Value, Entity) => from 3 to 11 March
	------------------------------

List(Assignment, Event) => sowing dates ranged from 14 to 31 July
	------------------------------
	Rule => Assignment-range-1
	Type => EventMention
	------------------------------
	trigger => ranged
	variable (Variable, Entity) => sowing dates
	value (Value, Entity) => from 14 to 31 July
	------------------------------
```

### 2.2. Batch mode

The same code can be called in batch mode with the command:

```
./var-read
```

This command expects a collection of documents, each saved as a `.txt` file in the directory `in/`. The software produces its output in the directory `out/`, using two formats. The first output files is called `mentions.tsv`, and it contains a tab-separated output. Similarly, it produces a `mentions.json` file, which contains the same output in JSON format, which might be more suitable for programmatic ingestion. As a simple example, let's assume that the `in/` directory contains a single file called `1.txt`, which contains the same text as the above example:

> Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS.

Running the `./var-read` command produces two files in the `out/` directory: `mentions.tsv` and `mentions.json`, where the former contains:

```
sowing dates    from 3 to 11 March      XXXX-03-03 -- XXXX-03-11        Farmers ’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS . 1.txt   N/A     N/A     N/A     N/A     N/A     N/A     N/A     N/A     N/A
sowing dates    from 14 to 31 July      XXXX-07-14 -- XXXX-07-31        Farmers ’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS . 1.txt   N/A     N/A     N/A     N/A     N/A     N/A     N/A     N/A     N/A
```

The `mentions.json` file contains:

```
[
  {
    "variableText" : "sowing dates",
    "valueText" : "from 3 to 11 March",
    "valueNorm" : "XXXX-03-03 -- XXXX-03-11",
    "sentenceText" : "Farmers ’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS .",
    "inputFilename" : "1.txt",
    "mostFreqLoc0Sent" : "N/A",
    "mostFreqLoc1Sent" : "N/A",
    "mostFreqLoc" : "N/A",
    "mostFreqDate0Sent" : "N/A",
    "mostFreqDate1Sent" : "N/A",
    "mostFreqDate" : "N/A",
    "mostFreqCrop0Sent" : "N/A",
    "mostFreqCrop1Sent" : "N/A",
    "mostFreqCrop" : "N/A"
  },
  {
    "variableText" : "sowing dates",
    "valueText" : "from 14 to 31 July",
    "valueNorm" : "XXXX-07-14 -- XXXX-07-31",
    "sentenceText" : "Farmers ’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS .",
    "inputFilename" : "1.txt",
    "mostFreqLoc0Sent" : "N/A",
    "mostFreqLoc1Sent" : "N/A",
    "mostFreqLoc" : "N/A",
    "mostFreqDate0Sent" : "N/A",
    "mostFreqDate1Sent" : "N/A",
    "mostFreqDate" : "N/A",
    "mostFreqCrop0Sent" : "N/A",
    "mostFreqCrop1Sent" : "N/A",
    "mostFreqCrop" : "N/A"
  }
]
```

The description of the columns in the `.tsv` file (or the equivalent fields in the `.json` file is:
TODO Mithun.

### 2.3. Programmatic access

The key class for variable reading is [`org.clulab.habitus.variables.VariableProcessor`](https://github.com/clulab/habitus/blob/main/src/main/scala/org/clulab/habitus/variables/VariableProcessor.scala).
Instantiate it with the `apply` method, e.g.: `val vp = VariableProcessor`. The key method is `parse`, which produces a tuple with four elements as follows:

1. The first tuple element is a `Document`, which contains all sentences extracted from the corresponding text as well as various NLP preprocessing such as part-of-speech (POS) tagging, dependency parsing, and semantic roles.
2. The second element is a list of *all* extractions from this document.
3. The third element is the actual list of event mentions, where each mention associates one variable with one value (see example above).
4. The last element is a histogram of context elements (e.g., locations, years) and distance from event mentions.

For an example on how these data structures are used, take a look at the method [`org.clulab.habitus.variables.VariableReader.run`](https://github.com/clulab/habitus/blob/main/src/main/scala/org/clulab/habitus/variables/VariableReader.scala#L25).

## 3. Reading for propositional attitudes

This component reads for statements such as WHO believes WHAT. TODO Mihai.
