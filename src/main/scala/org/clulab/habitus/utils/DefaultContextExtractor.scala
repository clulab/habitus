package org.clulab.habitus.utils

import org.clulab.odin.{Mention, TextBoundMention}
import org.clulab.processors.Document

import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention] = {
    // all mentions (tbms and relations/events) have to be passed here because context info comes from tbms;
    // only return contextualized relation and event mentions
    val toReturn = new ArrayBuffer[Mention]()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      val thisSentMentions = mentionsBySentence(i).distinct
      val (thisSentTBMs, thisSentEvents) = thisSentMentions.partition(_.isInstanceOf[TextBoundMention])
      val thisSentDates = thisSentTBMs.filter(m => m.label == "Date" || m.label == "DateRange")
      val thisSentLocs = thisSentTBMs.filter(_.label == "Location")
      val thisSentCrops = thisSentTBMs.filter(_.label == "Crop")
      val thisSentFerts = thisSentTBMs.filter(_.label == "Fertilizer")
      val thisSentSeason = thisSentTBMs.filter(_.label == "Season")
      for (m <- thisSentEvents) {
        // make a map of arg labels and texts for automatic context field assignment in cases where context is part of the mention itself
        val menArgLabels = m.arguments
          .flatMap(a => a._2)
          .map(men => men.label -> men.text).toMap
        val context = DefaultContext(
          getContext(m, "Location", thisSentLocs, mentions),
          getContext(m, "Date", thisSentDates, mentions),
          getProcess(m),
          // with crops and fertilizer (and maybe later other types of context), if a crop or fertilizer is one of the arguments,
          // ...just pick those to fill the context fields
          if (menArgLabels.contains("Crop")) menArgLabels("Crop") else getContext(m, "Crop", thisSentCrops, mentions),
          if (menArgLabels.contains("Fertilizer")) menArgLabels("Fertilizer") else getContext(m, "Fertilizer", thisSentFerts, mentions),
          getContext(m, "Season", thisSentSeason, mentions),
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