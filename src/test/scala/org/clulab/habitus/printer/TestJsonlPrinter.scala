package org.clulab.habitus.printer

import org.clulab.habitus.utils.{DefaultContext, TempFile, Test}
import org.clulab.utils.FileUtils

import scala.util.Using

class TestJsonlPrinter extends Test {

  behavior of "JsonlPrinter"

  it should "print one Mention with one argument" in {
    Using.resource(new TempFile()) { tempFile =>
      Using.resource(new JsonlPrinter(tempFile.file)) { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.", "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("publicationYear", "location", "country", "date", "process", "crop", "fertilizer", "season", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name", "text", "norm")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val expectedOutput =
          "{" +
            """"contextWindow":"This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",""" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"mentionText":"This is mention text",""" +
            """"context":{""" +
              """"publicationYear":"publicationYear",""" +
              """"location":"location",""" +
              """"country":"country",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
              """"season":"season","""+
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
    Using.resource(new TempFile()) { tempFile =>
      Using.resource(new JsonlPrinter(tempFile.file)) { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.", "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("publicationYear", "location", "country", "date", "process", "crop", "fertilizer", "season", 0)
        val argumentInfos = Seq(
          ArgumentInfo("name1", "text1", "norm1"),
          ArgumentInfo("name2", "text2", "norm2")
        )
        printer.outputInfos(mentionInfo, contextInfo, argumentInfos)
      }
      val expectedOutput =
          "{" +
            """"contextWindow":"This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",""" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"mentionText":"This is mention text",""" +
            """"context":{""" +
              """"publicationYear":"publicationYear",""" +
              """"location":"location",""" +
              """"country":"country",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
            """"season":"season","""+
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
    Using.resource(new TempFile()) { tempFile =>
      Using.resource(new JsonlPrinter(tempFile.file)) { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.", "This is the text of the sentence.", "It came from a file.", "The mention has this label.", "This is mention text")
        val contextInfo = DefaultContext("publicationYear", "location", "country", "date", "process", "crop", "fertilizer", "season", 0)
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
            """"contextWindow":"This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",""" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"mentionText":"This is mention text",""" +
            """"context":{""" +
              """"publicationYear":"publicationYear",""" +
              """"location":"location",""" +
              """"country":"country",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
            """"season":"season","""+
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
            """"contextWindow":"This is the text of the sentence before the current one. This is the text of the sentence. This is the text of the sentence after the current one.",""" +
            """"sentenceText":"This is the text of the sentence.",""" +
            """"inputFilename":"It came from a file.",""" +
            """"label":"The mention has this label.",""" +
            """"mentionText":"This is mention text",""" +
            """"context":{""" +
              """"publicationYear":"publicationYear",""" +
              """"location":"location",""" +
              """"country":"country",""" +
              """"date":"date",""" +
              """"process":"process",""" +
              """"crop":"crop",""" +
              """"fertilizer":"fertilizer",""" +
            """"season":"season","""+
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
