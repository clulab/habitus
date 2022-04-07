package org.clulab.habitus.printer

import org.clulab.habitus.utils.{DefaultContext, TempFile, Test}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

class TestJsonlPrinter extends Test {

  behavior of "JsonlPrinter"

  it should "print one Mention" in {
    new TempFile().autoClose { tempFile =>
      new JsonlPrinter(tempFile.file).autoClose { printer =>
        val mentionInfo = new MentionInfo("This is the text of the sentence.", "It came from a file.")
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
}
