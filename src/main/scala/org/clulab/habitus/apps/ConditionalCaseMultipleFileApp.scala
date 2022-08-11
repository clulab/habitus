package org.clulab.habitus.apps

import org.clulab.utils.FileUtils

/** Restore the case conditionally, and compare preserved and restored versions. */
object ConditionalCaseMultipleFileApp extends ConditionalCaseBase with App {
  val inputDir = args(0)

  val files = FileUtils.findFiles(inputDir, ".txt")

  process(files)
}
