package org.clulab.habitus.utils

import org.clulab.odin.{Mention, TextBoundMention}
import org.clulab.processors.Document

import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention] = {
    // TODO: ALL mentions have to be passed here to get location/date tbms
    //TODO: write a couple of context tests
    val toReturn = new ArrayBuffer[Mention]()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      val thisSentMentions = mentionsBySentence(i).distinct
      val (thisSentTBMs, thisSentEvents) = thisSentMentions.partition(_.isInstanceOf[TextBoundMention])
      val thisSentDates = thisSentTBMs.filter(_.label == "Date")
      val thisSentLocs = thisSentTBMs.filter(_.label == "Location")
      val thisSentCrops = thisSentTBMs.filter(_.label == "Crop")
      val thisSentFerts = thisSentTBMs.filter(_.label == "Fertilizer")
      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      for (m <- thisSentEvents) {
        val context = DefaultContext(
          getContext(m, "Location", thisSentLocs, mentions),
          getContext(m, "Date", thisSentDates, mentions),
          getProcess(m),
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