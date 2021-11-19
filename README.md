# habitus

This repository contains CLU lab's NLP software for the DARPA HEURISTICS project.

## 1. Requirements

This software requires:
- Java 8 
- sbt 1.x

## 2. Variable reading

This component reads for values assigned to variables that are important for crop modeling such as planting date and fertilizer usage. For example, from the sentence "Sowing between October 4 and October 14 was optimal." our software extracts the variable "sowing" with the value being the date range "between October 4 and October 14", which is normalized to "XXXX-10-04 -- XXXX-10-14". 

This component can be used in three different ways, as described below.

### 2.1. Read-eval-print loop (REPL)

We provide an interactive REPL interface, in which users can type natural language and inspect the structured output produced by the variable reader code. To call it, type:

```
./var-shell
```

For example, typing the sentence "Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS." (without the quotes), produces the following output:

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



## 3. Reading for propositional attitudes

This component reads for statements such as WHO believes WHAT.
