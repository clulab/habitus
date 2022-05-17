package org.clulab.habitus.entitynetworks

import java.io.File

/** extracts the NER from a single given text file and saves it in the same folder with the suffix .out. */
object LexiconNer extends LexiconNerBase with App {
  val inputFileName = args(0)

  parseFile(new File(inputFileName))
}
