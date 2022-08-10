package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.struct.Interval

import scala.collection.immutable.ListMap
import scala.util.control.Breaks._
import scala.collection.{breakOut, mutable}
import scala.collection.mutable.ArrayBuffer

trait Context extends Attachment with Pairable

case class DefaultContext(location: String, date: String, process: String, crop: String, fertilizer: String, season:String, comparative: Int) extends Context

case class BeliefContext(context: String) extends Context

trait ContextExtractor {

  val NA = "N/A"
  val maxContextWindow = 2

//  val plantingLemmas = Seq("plant", "sow", "cultivate", "cultivation", "grow")
//  val creditLemmas = Seq("credit", "finance", "value")
//  val harvestLemmas = Seq("harvest", "yield")
//  val irrigationLemmas = Seq("irrigation", "irrigate")
//  val weedsLemmas = Seq("weed")
//  val disasterLemmas = Seq("flood", "bird", "attack")
val processToLemmas = ListMap(
  "planting"         -> Set("plant", "sow", "cultivate", "cultivation", "grow", "seed", "seeding", "seedling", "transplant", "cropping", "variety"),
  "harvesting"       -> Set("harvest", "yield"),
  "credit"           -> Set("credit", "finance", "value"),
  "irrigation"       -> Set("irrigation", "irrigate"),
  "weeds"            -> Set("weed"),
  "natural_disaster" -> Set("flood", "bird", "attack", "floodwater"),
  "fertilizerApplication" -> Set("fertilizer", "application", "apply", "compost")
)

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention]

  def getProcess(mention: Mention): String = {

//    Getting process lemma from the mention
//    val seasonLemmas = seasonToLemmas.map(seasonLemma => seasonLemma._2.intersect(mention.lemmas.get.toSet).toList)
//    println(seasonLemmas)

    // get a wider window of lemmas here from sentenceObj
    val mentionStartIndex = mention.tokenInterval.start
    val mentionEndIndex = mention.tokenInterval.end
    val windowSize = 10
    val minusWindowStartIndex = mentionStartIndex - windowSize
    val plusWindowStartIndex = mentionEndIndex + windowSize
    val lemmas = mention.sentenceObj.lemmas.get.toSet.toList.slice(minusWindowStartIndex, plusWindowStartIndex)
    println(lemmas)

    val lemmaOverlapCounts = processToLemmas.map(p => (p._1, p._2.intersect(lemmas.toSet).toList.length))
    println(lemmaOverlapCounts)
    // find max overlap
    val maxOverlap = lemmaOverlapCounts.values.max
    println(maxOverlap)
    // filter out processes in lemmaOverlapCounts that are less than maximum overlap
    lemmaOverlapCounts.filter(_._2 == maxOverlap).keys.mkString("::")
  }

  def getComparative(mention: Mention): Int = {
    val relative = Seq("vs", "vs.", "respectively")
    if (relative.exists(mention.sentenceObj.words.contains(_))) 1 else 0
  }

  def getContext(m: Mention, contextType: String, contextRelevantMentions: Seq[Mention], allMentions: Seq[Mention]): String = {
    // for locations, take the closest next location; for the rest,
    // if no relevant context mentions in sentence, use the most freq one in sentence window equal to +/- maxContextWindow
    val context = contextRelevantMentions.length match {
      case 0 => getMostFrequentInContext(m, contextType, maxContextWindow, allMentions)
      case 1 => contextRelevantMentions.head.text
      case _ => {
        contextType match {
          case "Location" => {
            val nextLoc = findClosestNextLocation(m, contextRelevantMentions)
            if (nextLoc.isDefined) nextLoc.get.text else NA
          }
          case _ => findClosest(m, contextRelevantMentions).text
        }
      }
    }
    context
  }

  def getSentIDsInSpan(m: Mention, sentenceSpan: Int): Interval = {
    //provides sentence window interval for a given mention
    val docSents = m.document.sentences
    val currentSent = m.sentence
    val contextSpanStart = if (currentSent - sentenceSpan >= 0) currentSent - sentenceSpan else currentSent
    val contextSpanEnd = if (currentSent + sentenceSpan < docSents.length) currentSent + sentenceSpan + 1 else docSents.length
    Interval(contextSpanStart, contextSpanEnd)
  }
  def getInstancesInContext(contextType: String, contextSentences: Interval, allMentions: Seq[Mention]): Seq[String] = {
    val instances = {
        val relevantContextMentions = allMentions.filter(_.labels.contains(contextType))
        relevantContextMentions.filter(m => contextSentences.intersect(Seq(m.sentence)).nonEmpty).map(_.text)
      }
    instances
  }

  def getMostFrequentInContext(mention: Mention, contextType: String, maxWindow: Int, allMentions: Seq[Mention]): String = {
    for (windowSize <- 1 to maxWindow) {
      val contextSentences = getSentIDsInSpan(mention, windowSize)
      val instances = getInstancesInContext(contextType, contextSentences, allMentions)
      if (instances.nonEmpty) {
        return instances.groupBy(identity).map(i => i._1 -> i._2.length).maxBy(_._2)._1
      }
     }
    NA
  }

  def findClosestNextLocation(mention: Mention, locations: Seq[Mention]): Option[Mention] = {
    if (locations.length == 1) return Some(locations.head)
    val nextLocations = locations.filter(_.tokenInterval.start > mention.arguments("value").head.tokenInterval.start)
    if (nextLocations.nonEmpty) Some(nextLocations.minBy(_.tokenInterval))
    else None
  }

  def getDistance(m1: Mention, m2: Mention): Int = {
    val sorted = Seq(m1, m2).sortBy(_.tokenInterval)
    sorted.last.tokenInterval.start - sorted.head.tokenInterval.end
  }

  def findClosest(mention: Mention, mentions: Seq[Mention]): Mention = {
    mentions.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
  }

}