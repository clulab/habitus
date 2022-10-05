package org.clulab.habitus.utils

import ai.lum.common.ConfigFactory
import ai.lum.common.ConfigUtils._
import com.typesafe.config.Config
import org.clulab.odin.{Mention, TextBoundMention}
import org.clulab.processors.Document
import org.clulab.utils.FileUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

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
    // all mentions (tbms and relations/events) have to be passed here because context info comes from tbms;
    // only return contextualized relation and event mentions
    val toReturn = new ArrayBuffer[Mention]()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {
      val sentLemmas = s.lemmas.getOrElse(Array.empty)
      val thisSentMentions = mentionsBySentence(i).distinct
      val (thisSentTBMs, thisSentEvents) = thisSentMentions.partition(_.isInstanceOf[TextBoundMention])
      val thisSentDates = thisSentTBMs.filter(m => m.label == "Date" || m.label == "DateRange")
      val thisSentLocs = thisSentTBMs.filter(_.label == "Location")
      val thisSentCrops = thisSentTBMs.filter(_.label == "Crop")
      val thisSentFerts = thisSentTBMs.filter(_.label == "Fertilizer")
      val thisSentSeason = thisSentTBMs.filter(_.label == "Season")
      for (m <- thisSentEvents) {
        // make a map of arg labels and texts for automatic context field assignment in cases where context is part of the mention itself
        val menArgLabels = m.arguments.values.flatten
          .map(men => men.label -> men.text).toMap
        val context = if (sentLemmas.contains("respectively")) {
          // for context type
          // if number of given context type = number of mentions in the sentence of the same label,
          // then do pairwise context The groundnut and the Fleur 11 gave higher yield in DELTA with 4.5 and 4.2 t ha-1 , respectively ( Table 4 ) .
          // else do regular context
          val locationContext = if (contextPairedWithMention(m, thisSentLocs, thisSentMentions)) doPairwiseContextMatching(m, thisSentLocs, thisSentMentions) else getContext(m, "Location", thisSentLocs, mentions)
          val countryContext = DefaultContextExtractor.regionMap.getOrElse(locationContext, "N/A")// todo: lookup location in region-country map
          DefaultContext(
            locationContext,
            countryContext,
            if (contextPairedWithMention(m, thisSentDates, thisSentMentions)) doPairwiseContextMatching(m, thisSentDates, thisSentMentions) else getContext(m, "Date", thisSentDates, mentions),
            getProcess(m),
            // with crops and fertilizer (and maybe later other types of context), if a crop or fertilizer is one of the arguments,
            // ...just pick those to fill the context fields
            if (contextPairedWithMention(m, thisSentCrops, thisSentMentions)) doPairwiseContextMatching(m, thisSentCrops, thisSentMentions) else getContext(m, "Crop", thisSentCrops, mentions),
            if (contextPairedWithMention(m, thisSentFerts, thisSentMentions)) doPairwiseContextMatching(m, thisSentFerts, thisSentMentions) else getContext(m, "Fertilizer", thisSentFerts, mentions),
            if (contextPairedWithMention(m, thisSentSeason, thisSentMentions)) doPairwiseContextMatching(m, thisSentSeason, thisSentMentions) else getContext(m, "Season", thisSentSeason, mentions),
            getComparative(m)
          )
        } else {
          val locationContext = getContext(m, "Location", thisSentLocs, mentions)
          val countryContext = DefaultContextExtractor.regionMap.getOrElse(locationContext, "N/A")// todo: lookup location in region-country map
          DefaultContext(
            locationContext,
            countryContext,
            if (menArgLabels.exists(_._1 contains "Date")) menArgLabels.filter(_._1 contains "Date").head._2 else  getContext(m, "Date", thisSentDates, mentions),
            getProcess(m),
            // with crops and fertilizer (and maybe later other types of context), if a crop or fertilizer is one of the arguments,
            // ...just pick those to fill the context fields
            if (menArgLabels.contains("Crop")) menArgLabels("Crop") else getContext(m, "Crop", thisSentCrops, mentions),
            if (menArgLabels.contains("Fertilizer")) menArgLabels("Fertilizer") else getContext(m, "Fertilizer", thisSentFerts, mentions),
            getContext(m, "Season", thisSentSeason, mentions),
            getComparative(m)
          )
        }

        // store context as a mention attachment
        val withAtt = m.withAttachment(context)
        toReturn.append(withAtt)
      }
    }
    toReturn.distinct
  }
}

object DefaultContextExtractor {

  val config = ConfigFactory.load()
  val localConfig: Config = config[Config]("VarReader")
  val inputDir: String = localConfig[String]("regionsLexicon")
  val files = FileUtils.findFiles(inputDir, ".tsv")
  val regionMap = mutable.Map[String, String]()
  files.foreach { f =>
    val source = Source.fromFile(f)
    for (line <- source.getLines()) {
      val split = line.trim.split("\t").tail
      regionMap += (split.head -> split.last)

    }
  }
}