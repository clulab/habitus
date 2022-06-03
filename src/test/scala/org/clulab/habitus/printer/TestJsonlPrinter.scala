package org.clulab.habitus.printer

import org.clulab.habitus.utils.{DefaultContext, TempFile, Test}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

class TestJsonlPrinter extends Test {

  behavior of "JsonlPrinter"

  it should "print one Mention with one argument" in {
    new TempFile().autoClose { tempFile =>
      new JsonlPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.", "The mention has this label.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name", "text", "norm")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val expectedOutput =
          "{" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"context":{""" +
              """"location":"location",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
              """"comparative":0""" +
            "}," +
            """"arguments":[{""" +
              """"name":"name",""" +
              """"text":"text",""" +
              """"norm":"norm"""" +
            "}]" +
          "}"
      val actualOutput = FileUtils.getTextFromFile(tempFile.file).trim

      actualOutput should be (expectedOutput)
    }
  }

  it should "print one Mention with two arguments" in {
    new TempFile().autoClose { tempFile =>
      new JsonlPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.", "The mention has this label.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name1", "text1", "norm1"),
          ArgumentInfo("name2", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val expectedOutput =
          "{" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"context":{""" +
              """"location":"location",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
              """"comparative":0""" +
            "}," +
            """"arguments":[{""" +
              """"name":"name1",""" +
              """"text":"text1",""" +
              """"norm":"norm1"""" +
            "},{" +
              """"name":"name2",""" +
              """"text":"text2",""" +
              """"norm":"norm2"""" +
            "}]" +
          "}"
      val actualOutput = FileUtils.getTextFromFile(tempFile.file).trim

      actualOutput should be (expectedOutput)
    }
  }

  it should "print two Mentions" in {
    new TempFile().autoClose { tempFile =>
      new JsonlPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.", "The mention has this label.")
        val contextInfo = DefaultContext("location", "date", "process", "crop", "fertilizer", 0)
        val argumentInfos1 = Seq(
          ArgumentInfo("name1", "text1", "norm1")
        )
        val argumentInfos2 = Seq(
          ArgumentInfo("name2", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos1)
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos2)
      }
      val expectedOutput1 =
          "{" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"context":{""" +
              """"location":"location",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
              """"comparative":0""" +
            "}," +
            """"arguments":[{""" +
              """"name":"name1",""" +
              """"text":"text1",""" +
              """"norm":"norm1"""" +
            "}]" +
          "}"
      val expectedOutput2 =
          "{" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"context":{""" +
              """"location":"location",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
              """"comparative":0""" +
            "}," +
            """"arguments":[{""" +
              """"name":"name2",""" +
              """"text":"text2",""" +
              """"norm":"norm2"""" +
            "}]" +
          "}"
      val expectedOutput = Seq(expectedOutput1, expectedOutput2).mkString("", "\n", "\n")
      val actualOutput = FileUtils.getTextFromFile(tempFile.file)

      actualOutput should be (expectedOutput)
    }
  }
}
