package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}

import scala.util.Using

object HistogramCausalBeliefs extends App with Logging {
  val inputFileName = "../corpora/multimix/dataset55k.tsv"
  val expectedColumnCount = 22
  val years = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val firstLine = lines.next

    var causal = 0
    var belief = 0
    var both = 0
    var neither = 0

    lines.foreach { line =>
      val columns = line.split('\t')
      assert(columns.length == expectedColumnCount)
      val causalIndex = columns(6)

      if (causalIndex == "" || causalIndex == "0") {
        val isCausal = causalIndex == "0"
        val isBelief = columns(18) == "True"

        if (isCausal) causal += 1
        if (isBelief) belief += 1
        if (isCausal && isBelief) both += 1
        if (!isCausal && !isBelief) neither += 1
      }
    }
    println(s" causal = $causal")
    println(s" belief = $belief")
    println(s"   both = $both")
    println(s"neither = $neither")
  }
}
