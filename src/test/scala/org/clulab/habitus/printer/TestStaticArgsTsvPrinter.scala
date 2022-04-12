package org.clulab.habitus.printer

import org.clulab.habitus.utils.{DefaultContext, TempFile, Test}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

class TestStaticArgsTsvPrinter extends Test {

  behavior of "StaticArgsTsvPrinter"

  it should "print one Mention with one argument" in {
    new TempFile().autoClose { tempFile =>
      new StaticArgsTsvPrinter(tempFile.file).autoClose { printer =>
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
        "name_text", "name_norm"
      ).mkString("\t")
      val valueOutput = Seq(
        "This is the text of the sentence.", "It came from a file.",
        "location", "date", "process", "crop", "fertilizer", "0",
        "text", "norm"
      ).mkString("\t")
      val expectedOutput = Seq(headerOutput, valueOutput).mkString("", "\n", "\n")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }

  it should "print one Mention with two arguments" in {
    new TempFile().autoClose { tempFile =>
      new StaticArgsTsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name1", "text1", "norm1"),
          ArgumentInfo("name2", "text2", "norm2"),
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val headerOutput = Seq(
        "sentenceText", "inputFilename",
        "location", "date", "process", "crop", "fertilizer", "comparative",
        "name1_text", "name1_norm", "name2_text", "name2_norm"
      ).mkString("\t")
      val valueOutput = Seq(
        "This is the text of the sentence.", "It came from a file.",
        "location", "date", "process", "crop", "fertilizer", "0",
        "text1", "norm1",
        "text2", "norm2",
      ).mkString("\t")
      val expectedOutput = Seq(headerOutput, valueOutput).mkString("", "\n", "\n")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }

  it should "print two Mentions with the same arguments" in {
    new TempFile().autoClose { tempFile =>
      new StaticArgsTsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos1 = Seq(
          ArgumentInfo("name", "text1", "norm1")
        )
        val argumentInfos2 = Seq(
          ArgumentInfo("name", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos1)
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos2)
      }
      val headerOutput = Seq(
        "sentenceText", "inputFilename",
        "location", "date", "process", "crop", "fertilizer", "comparative",
        "name_text", "name_norm"
      ).mkString("\t")
      val valueOutput1 = Seq(
        "This is the text of the sentence.", "It came from a file.",
        "location", "date", "process", "crop", "fertilizer", "0",
        "text1", "norm1"
      ).mkString("\t")
      val valueOutput2 = Seq(
        "This is the text of the sentence.", "It came from a file.",
        "location", "date", "process", "crop", "fertilizer", "0",
        "text2", "norm2"
      ).mkString("\t")
      val expectedOutput1 = Seq(headerOutput, valueOutput1).mkString("", "\n", "\n")
      val expectedOutput2 = Seq(              valueOutput2).mkString("", "\n", "\n")
      val expectedOutput = Seq(expectedOutput1, expectedOutput2).mkString("", "", "")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }

  it should "not print two Mentions with different arguments" in {
    new TempFile().autoClose { tempFile =>
      new StaticArgsTsvPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos1 = Seq(
          ArgumentInfo("name1", "text1", "norm1")
        )
        val argumentInfos2 = Seq(
          ArgumentInfo("name2", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos1)

        a [RuntimeException] should be thrownBy
          printer.outputInfos(mentionInfo, contextInfo, argumentInfos2)
      }
    }
  }
}
