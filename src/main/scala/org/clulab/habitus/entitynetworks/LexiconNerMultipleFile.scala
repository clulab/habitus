package org.clulab.habitus.entitynetworks

import org.clulab.utils.FileUtils

/** extracts the NER from multiple given text files in a folder and saves them in the same folder with the suffix .out. */
object LexiconNerMultipleFile extends LexiconNerBase with App {
  val inputDirName = args(0)
  val inputFiles = FileUtils.findFiles(inputDirName, ".txt.restored")

  inputFiles.foreach(parseFile)
}
