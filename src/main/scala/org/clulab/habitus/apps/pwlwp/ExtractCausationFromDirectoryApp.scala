package org.clulab.habitus.apps.pwlwp

import org.clulab.habitus.utils.TsvWriter
import org.clulab.wm.eidoscommon.utils.{FileUtils, Logging}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject, JValue}

import scala.util.Using

object ExtractCausationFromDirectoryApp extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 2

  val inputDir = args(0)
  val outputFile = args(1)

  val files = FileUtils.findFiles(inputDir, "jsonld")

  Using.resource(FileUtils.printWriterFromFile(outputFile)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)

    tsvWriter.println("file", "causation", "sentence", "context")
    files.foreach { file =>
      try {
        val json = FileUtils.getTextFromFile(file)
        val root = JsonMethods.parse(json).extract[JObject]
        val documents = {
          val documents = (root \ "documents").extract[JArray]

          require(documents.arr.length == 1)
          documents
        }
        val sentences = (documents(0) \ "sentences").extractOpt[JArray]
            .map(_.arr)
            .getOrElse(List.empty[JValue])
        val sentenceIdAndTextTuples = sentences.map { sentence =>
          val id = (sentence \ "@id").extract[String]
          val text = (sentence \ "text").extract[String]

          id -> text
        }

        val extractions = (root \ "extractions").extractOpt[JArray]
            .map(_.arr)
            .getOrElse(List.empty[JValue])
        val causations = extractions.filter { extraction =>
          (extraction \ "subtype").extract[String] == "causation"
        }
        val textSentenceContextTuples = causations.map { causation =>
          val text = (causation \ "text").extract[String]
          val provenances = {
            val provenances = (causation \ "provenance").extract[JArray]

            require(provenances.arr.length == 1)
            provenances
          }
          val causationSentenceId = (provenances(0) \ "sentence" \ "@id").extract[String]
          val sentenceIndex = sentenceIdAndTextTuples.indexWhere { case (sentenceId, _) =>
            causationSentenceId == sentenceId
          }
          val sentence = sentenceIdAndTextTuples(sentenceIndex)._2
          val contextRange = Range.inclusive(sentenceIndex - contextWindow, sentenceIndex + contextWindow)
          val contextSentences = contextRange.flatMap { sentenceIndex =>
            val lifted = sentenceIdAndTextTuples.lift(sentenceIndex).map { sth => sth._2 }

            lifted
          }
          val context = contextSentences.mkString(" ")

          (text, sentence, context)
        }

        textSentenceContextTuples.foreach { case (text, sentence, context) =>
          tsvWriter.println(file.getName, text, sentence, context)
        }
      }
      catch {
        case exception: Exception =>
          logger.error(s"Exception for file $file", exception)
      }
    }
  }
}
