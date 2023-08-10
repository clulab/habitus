package org.clulab.habitus.apps

import org.clulab.wm.eidos.serialization.jsonld.{JLDDeserializer, JLDRelationCausation}
import org.clulab.wm.eidoscommon.utils.{FileUtils, Logging, TsvWriter}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JArray, JObject, JValue}

import scala.util.Using

object ExtractCausalSentencesFromDirectoryApp extends App with Logging {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
  val contextWindow = 2

  val inputDir = args(0)
  val outputFile = args(1)

  val files = FileUtils.findFiles(inputDir, "jsonld")

  Using.resource(FileUtils.printWriterFromFile(outputFile)) { printWriter =>
    val tsvWriter = new TsvWriter(printWriter)
    val deserializer = new JLDDeserializer()

    tsvWriter.println("file", "index", "sentence", "causal")
    files.foreach { file =>
      try {
        val json = FileUtils.getTextFromFile(file)
        val corpus = deserializer.deserialize(json)
        val annotatedDocument = corpus.head
        val document = annotatedDocument.document
        val documentText = document.text.get
        val sentences = document.sentences
        val allMentions = annotatedDocument.allEidosMentions
        val causalMentions = allMentions.filter(_.label == JLDRelationCausation.taxonomy)
        val causalSentenceIndices = causalMentions.map(_.odinMention.sentence).toSet

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val causal = causalSentenceIndices(sentenceIndex)
          val rawText = documentText.slice(sentence.startOffsets.head, sentence.endOffsets.last)

          tsvWriter.println(file.getName, sentenceIndex.toString, rawText, causal.toString)
        }
      }
      catch {
        case exception: Exception =>
          logger.error(s"Exception for file $file", exception)
      }
    }
  }
}
