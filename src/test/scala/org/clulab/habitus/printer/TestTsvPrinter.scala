package org.clulab.habitus.printer

import org.clulab.habitus.utils.DefaultContext
import org.clulab.habitus.utils.{TempFile, Test}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

class TestTsvPrinter extends Test {

  behavior of "TsvPrinter"

  it should "print one Mention" in {
    new TempFile().autoClose { tempFile =>
      new TsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name", "text", "norm")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val headerOutput = Seq(
        "sentenceText", "inputFilename",
        "location", "date", "process", "crop", "fertilizer", "comparative",
        "arg0_name", "arg0_text", "arg0_norm", "..."
      ).mkString("\t")
      val valueOutput = Seq(
        "This is the text of the sentence.", "It came from a file.",
        "location", "date", "process", "crop", "fertilizer", "0",
        "name", "text", "norm"
      ).mkString("\t")
      val expectedOutput = Seq(headerOutput, valueOutput).mkString("\n")

      val actualOutput = FileUtils.getTextFromFile(tempFile.file).trim

      actualOutput should be (expectedOutput)
    }
  }
}
