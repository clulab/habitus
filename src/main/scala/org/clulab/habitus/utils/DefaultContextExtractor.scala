package org.clulab.habitus.utils

import org.clulab.odin.{Mention, TextBoundMention}
import org.clulab.processors.Document

import scala.collection.mutable.ArrayBuffer

class DefaultContextExtractor extends ContextExtractor {

  val likelyClauseBreak = Seq("respectively", "whereas", "however", "while")

  def getMentionsWithinClause(current: Mention, mentions: Seq[Mention]): Seq[Mention] = {
    //get mentions that are not separated from the current one by sentence-break lemmas
    mentions.filter(m => {
      val interval = Seq(current, m).sortBy(_.tokenInterval)
      current.sentenceObj.lemmas.get.slice(interval.head.start, interval.last.end).intersect(likelyClauseBreak).isEmpty
    })
  }

  def contextPairedWithMention(current: Mention, relevantContextMentions: Seq[Mention], thisSentMentions: Seq[Mention]): Boolean = {
    val sameClauseMentions = getMentionsWithinClause(current, thisSentMentions)
    val sameClauseContextMentions = getMentionsWithinClause(current, relevantContextMentions)


    val sameMentionTypeCount = sameClauseMentions.count(_.label == current.label)
    // if there are more than one mention of this type and the number of context mentions of a given type match,
    // then there is a reason to believe that 'respectively' refered to that context type for this mention

    val result = sameMentionTypeCount > 1 && sameMentionTypeCount == sameClauseContextMentions.length
    result

  }

  def doPairwiseContextMatching(current: Mention, relevantContextMentions: Seq[Mention], thisSentMentions: Seq[Mention]): String = {
    // get index of current mention among mention of the same type
    // pick a corresponding index context
    val orderOfCurrentMentionAmongEquals = thisSentMentions.filter(_.label == current.label).sortBy(_.tokenInterval).zipWithIndex.filter(mz => mz._1.tokenInterval == current.tokenInterval).head._2
    relevantContextMentions.sortBy(_.tokenInterval).zipWithIndex.filter(_._2 == orderOfCurrentMentionAmongEquals).head._1.text
  }

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention] = {

    def getRespectiveContext(mention: Mention, label: String, labelMentions: Seq[Mention], sentMentions: Seq[Mention]): String = {
      // for context type
      // if number of given context type = number of mentions in the sentence of the same label,
      // then do pairwise context The groundnut and the Fleur 11 gave higher yield in DELTA with 4.5 and 4.2 t ha-1 , respectively ( Table 4 ) .
      // else do regular context
      if (contextPairedWithMention(mention, labelMentions, sentMentions))
        doPairwiseContextMatching(mention, labelMentions, sentMentions)
      else
        getContext(mention, label, labelMentions, mentions)
    }

    def getIrrespectiveContext(mention: Mention, label: String, labelMentions: Seq[Mention],
        menArgLabelsOpt: Option[Map[String, String]]): String = {
      menArgLabelsOpt
          .flatMap(_.get(label)) // Try menArgLabels first if it is provided.
          .getOrElse(getContext(mention, label, labelMentions, mentions))
    }

    // all mentions (tbms and relations/events) have to be passed here because context info comes from tbms;
    // only return contextualized relation and event mentions
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    val mentionsWithContext = doc.sentences.zipWithIndex.flatMap { case (s, i) =>
      val sentLemmas = s.lemmas.getOrElse(Array.empty)
      val thisSentMentions = mentionsBySentence(i).distinct
      val (thisSentTBMs, thisSentEvents) = thisSentMentions.partition(_.isInstanceOf[TextBoundMention])
      val thisSentDates = thisSentTBMs.filter(m => m.label == "Date" || m.label == "DateRange")
      val thisSentLocs = thisSentTBMs.filter(_.label == "Location")
      val thisSentCrops = thisSentTBMs.filter(_.label == "Crop")
      val thisSentFerts = thisSentTBMs.filter(_.label == "Fertilizer")
      val thisSentSeason = thisSentTBMs.filter(_.label == "Season")
      val sentEventsWithContext = thisSentEvents.map { m =>
        val context = if (sentLemmas.contains("respectively")) {
          val location   = getRespectiveContext(m, "Location",   thisSentLocs,   thisSentMentions)
          val date       = getRespectiveContext(m, "Date",       thisSentDates,  thisSentMentions)
          val crop       = getRespectiveContext(m, "Crop",       thisSentCrops,  thisSentMentions)
          val fertilizer = getRespectiveContext(m, "Fertilizer", thisSentFerts,  thisSentMentions)
          val season     = getRespectiveContext(m, "Season",     thisSentSeason, thisSentMentions)

          DefaultContext(location, date, getProcess(m), crop, fertilizer, season, getComparative(m))
        }
        else {
          // make a map of arg labels and texts for automatic context field assignment in cases where context is part of the mention itself
          val menArgLabels = m.arguments.values.flatten
              .map(men => men.label -> men.text).toMap
          val location   = getIrrespectiveContext(m, "Location",   thisSentLocs,   None)
          val date       = getIrrespectiveContext(m, "Date",       thisSentDates,  Some(menArgLabels))
          // with crops and fertilizer (and maybe later other types of context), if a crop or fertilizer is one of the arguments,
          // ...just pick those to fill the context fields
          val crop       = getIrrespectiveContext(m, "Crop",       thisSentCrops,  Some(menArgLabels))
          val fertilizer = getIrrespectiveContext(m, "Fertilizer", thisSentFerts,  Some(menArgLabels))
          val season     = getIrrespectiveContext(m, "Season",     thisSentSeason, None)

          DefaultContext(location, date, getProcess(m), crop, fertilizer, season, getComparative(m))
        }
        // store context as a mention attachment
        val withAtt = m.withAttachment(context)

        withAtt
      }

      sentEventsWithContext
    }
    mentionsWithContext.distinct
  }
}