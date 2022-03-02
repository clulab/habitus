package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.Mention
import org.clulab.processors.Document

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq], doc: Document, label: String): Seq[Mention] = {
    val toReturn = new ArrayBuffer[Mention]()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {
      val words=doc.sentences(i).words.mkString(" ")
      println(words)
      println(doc.sentences(i).tags)
      val thisSentMentions = mentionsBySentence(i).distinct
      val contentMentions = thisSentMentions.filter(_.label matches label)
      val thisSentDates = thisSentMentions.filter(_.label == "Date")
      val thisSentLocs = thisSentMentions.filter(_.label == "Location")
      val thisSentCrops = thisSentMentions.filter(_.label == "Crop")
      val thisSentFerts = thisSentMentions.filter(_.label == "Fertilizer")

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      for (m <- contentMentions) {
        println(m.words)
        println(m.tags)
        //get pos tags here for factuality
        // check inside the mention
        // if not check the sentence object.
        val context = DefaultContext(
          getContext(m, "Date", thisSentDates, mentions),
          getContext(m, "Location", thisSentLocs, mentions),
          getProcess(m),
//        getCropContext(m, frequencyContext),
          getContext(m, "Crop", thisSentCrops, mentions),
          getContext(m, "Fertilizer", thisSentFerts, mentions),
//          getContextFromHistogramInWindow(m, "Fertilizer", maxContextWindow, entityHistogram),
          getComparative(m),
          getFactualityScore(m)
        )

        // store context as a mention attachment
        val withAtt = m.withAttachment(context)
        toReturn.append(withAtt)
      }
    }
    toReturn.distinct
  }
}