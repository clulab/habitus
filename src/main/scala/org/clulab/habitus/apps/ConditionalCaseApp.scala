package org.clulab.habitus.apps

import java.io.File

/** Restore the case conditionally, and compare preserved and restored versions. */
object ConditionalCaseApp extends ConditionalCaseBase with App {
  val inputFileName = args(0)

  val files = Seq(new File(inputFileName))

  process(files)
}
