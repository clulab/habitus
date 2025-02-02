package org.clulab.habitus.apps.pwlwp

import org.clulab.habitus.utils.TsvWriter
import org.clulab.wm.eidos.serialization.jsonld.{JLDDeserializer, JLDRelationCausation}
import org.clulab.wm.eidoscommon.utils.{FileUtils, Logging}
import org.json4s.DefaultFormats

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
        // TODO: Should also be an event mention?
        val causalMentions = allMentions.filter(_.label == JLDRelationCausation.taxonomy)
        val causalSentenceIndices = causalMentions.map(_.odinMention.sentence).toSet

        sentences.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
          val causal = causalSentenceIndices(sentenceIndex)
          val rawText = documentText.slice(sentence.startOffsets.head, sentence.endOffsets.last)
          val cleanText = rawText
              .trim
              .replaceAll("\r\n", " ")
              .replaceAll("\n", " ")
              .replaceAll("\r", " ")
              .replaceAll("\t", " ")

          tsvWriter.println(file.getName, sentenceIndex.toString, cleanText, causal.toString)
        }
      }
      catch {
        case exception: Exception =>
          logger.error(s"Exception for file $file", exception)
      }
    }
  }
}
