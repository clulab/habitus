package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.Mention
import org.clulab.processors.Document

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq], doc: Document, label: String): Seq[Mention] = {
    val toReturn = new ArrayBuffer[Mention]()
    val frequencyContext = {
      if (entityHistogram.isEmpty) mutable.Map.empty[Int, ContextDetails]
      else compressContext(doc, mentions, entityHistogram)
    }.toMap
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      val thisSentMentions = mentionsBySentence(i).distinct
      val contentMentions = thisSentMentions.filter(_.label matches label)
      val thisSentDates = thisSentMentions.filter(_.label == "Date")
      val thisSentLocs = thisSentMentions.filter(_.label == "Location")

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      for (m <- contentMentions) {
        val context = DefaultContext(
          getDate(m, thisSentDates, frequencyContext, mentions),
          getLocation(m, thisSentLocs, frequencyContext),
          getProcess(m),
//          getCropContext(m, frequencyContext),
          getContextFromHistogramInWindow(m, "crop", maxContextWindow, entityHistogram),
          getContextFromHistogramInWindow(m, "fertilizer", maxContextWindow, entityHistogram),
          getComparative(m)
        )

        // store context as a mention attachment
        val withAtt = m.withAttachment(context)
        toReturn.append(withAtt)
      }
    }
    toReturn.distinct
  }
}