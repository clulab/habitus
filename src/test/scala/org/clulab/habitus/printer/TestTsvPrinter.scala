package org.clulab.habitus.printer

import org.clulab.habitus.utils.DefaultContext
import org.clulab.habitus.utils.{TempFile, Test}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

class TestTsvPrinter extends Test {

  behavior of "TsvPrinter"

  it should "print one Mention with one argument" in {
    new TempFile().autoClose { tempFile =>
      new TsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.","This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("location", "country", "date", "process", "crop", "fertilizer", "season", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name", "text", "norm")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val headerOutput = Seq(
        "contextWindow", "sentenceText", "inputFilename", "label", "mentionText",
        "location", "country", "date", "process", "crop", "fertilizer", "season", "comparative",
        "arg0_name", "arg0_text", "arg0_norm", "..."
      ).mkString("\t")
      val valueOutput = Seq(
        "This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",
        "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text",
        "location", "country", "date", "process", "crop", "fertilizer", "season", "0",
        "name", "text", "norm"
      ).mkString("\t")
      val expectedOutput = Seq(headerOutput, valueOutput).mkString("", "\n", "\n")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }

  it should "print one Mention with two arguments" in {
    new TempFile().autoClose { tempFile =>
      new TsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.","This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("location", "country", "date", "process", "crop", "fertilizer", "season", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name1", "text1", "norm1"),
          ArgumentInfo("name2", "text2", "norm2"),
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val headerOutput = Seq(
        "contextWindow", "sentenceText", "inputFilename", "label", "mentionText",
        "location", "country", "date", "process", "crop", "fertilizer", "season", "comparative",
        "arg0_name", "arg0_text", "arg0_norm", "..."
      ).mkString("\t")
      val valueOutput = Seq(
        "This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",
        "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text",
        "location", "country", "date", "process", "crop", "fertilizer", "season", "0",
        "name1", "text1", "norm1",
        "name2", "text2", "norm2",
      ).mkString("\t")
      val expectedOutput = Seq(headerOutput, valueOutput).mkString("", "\n", "\n")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }

  it should "print two Mentions" in {
    new TempFile().autoClose { tempFile =>
      new TsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.","This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("location", "country", "date", "process", "crop", "fertilizer", "season", 0)
        val argumentInfos1 = Seq(
          ArgumentInfo("name1", "text1", "norm1")
        )
        val argumentInfos2 = Seq(
          ArgumentInfo("name2", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos1)
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos2)
      }
      val headerOutput = Seq(
        "contextWindow", "sentenceText", "inputFilename", "label", "mentionText",
        "location", "country", "date", "process", "crop", "fertilizer", "season", "comparative",
        "arg0_name", "arg0_text", "arg0_norm", "..."
      ).mkString("\t")
      val valueOutput1 = Seq(
        "This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",
        "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text",
        "location", "country", "date", "process", "crop", "fertilizer", "season","0",
        "name1", "text1", "norm1"
      ).mkString("\t")
      val valueOutput2 = Seq(
        "This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",
        "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text",
        "location", "country", "date", "process", "crop", "fertilizer", "season","0",
        "", "", "", // This is the important part.  Skip these.
        "name2", "text2", "norm2",
      ).mkString("\t")
      val expectedOutput1 = Seq(headerOutput, valueOutput1).mkString("", "\n", "\n")
      val expectedOutput2 = Seq(              valueOutput2).mkString("", "\n", "\n")
      val expectedOutput = Seq(expectedOutput1, expectedOutput2).mkString("", "", "")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }
}
