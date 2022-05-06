package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document

import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention] = {
    // mentions here are already only relations and events
    val toReturn = new ArrayBuffer[Mention]()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      val thisSentMentions = mentionsBySentence(i).distinct
      val thisSentDates = thisSentMentions.filter(_.label == "Date")
      val thisSentLocs = thisSentMentions.filter(_.label == "Location")
      val thisSentCrops = thisSentMentions.filter(_.label == "Crop")
      val thisSentFerts = thisSentMentions.filter(_.label == "Fertilizer")
      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      for (m <- mentions) {
        val context = DefaultContext(
          getContext(m, "Date", thisSentDates, mentions),
          getContext(m, "Location", thisSentLocs, mentions),
          getProcess(m),
//          getCropContext(m, frequencyContext),
          getContext(m, "Crop", thisSentCrops, mentions),
          getContext(m, "Fertilizer", thisSentFerts, mentions),
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