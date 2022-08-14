package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.struct.Interval
import scala.util.control.Breaks._

import scala.collection.{breakOut, mutable}
import scala.collection.mutable.ArrayBuffer

trait Context extends Attachment with Pairable

case class DefaultContext(location: String, date: String, process: String, crop: String, fertilizer: String, comparative: Int) extends Context

case class BeliefContext(context: String) extends Context

trait ContextExtractor {

  val NA = "N/A"
  val maxContextWindow = 2

  val plantingLemmas: Seq[String] = Seq("plant", "sow", "cultivate", "cultivation", "grow")
  val creditLemmas: Seq[String] = Seq("credit", "finance", "value")
  val harvestLemmas: Seq[String] = Seq("harvest", "yield")
  val irrigationLemmas: Seq[String] = Seq("irrigation", "irrigate")
  val weedsLemmas: Seq[String] = Seq("weed")
  val disasterLemmas: Seq[String] = Seq("flood", "bird", "attack")

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention]

  def getProcess(mention: Mention): String = {

    // for checking verbal triggers
//    val stopVerbs = Seq("be", "go")
//    val sentVerbs = new ArrayBuffer[String]()
//    val sentLemmas = mention.sentenceObj.lemmas.get
//    for ((tag, i) <- mention.sentenceObj.tags.get.zipWithIndex) {
//      if (tag.startsWith("VB")) {
//        sentVerbs.append(sentLemmas(i))
//      }
//    }

    val lemmas = mention.sentenceObj.lemmas.get
    val process = if (lemmas.exists(l => plantingLemmas.contains(l))) {
      "planting"
    } else if (lemmas.exists(l => harvestLemmas.contains(l))) {
      "harvesting"
    } else if (lemmas.exists(l => creditLemmas.contains(l))) {
      "credit"
    } else if (lemmas.exists(l => irrigationLemmas.contains(l))) {
      "irrigation"
    } else if (lemmas.exists(l => weedsLemmas.contains(l))) {
      "weeds"
    } else if (lemmas.exists(l => disasterLemmas.contains(l))) {
      "natural_disaster"
    } else "UNK"
    process
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
          case "Date" => findClosestNotOverlapping(m, contextRelevantMentions).text
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
        val relevantContextMentions = allMentions.filter(_.label == contextType)
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

  def findClosestNotOverlapping(mention: Mention, mentions: Seq[Mention]): Mention = {
    // check if there are years that don't overlap with the mention itself and if yes, pick closest of those
    // if there are no non-intersecting mentions, just pick any nearest one
    val nonIntersecting = mentions.filter(m => m.tokenInterval.intersect(mention.tokenInterval).isEmpty)
    if (nonIntersecting.nonEmpty) {
      nonIntersecting.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
    } else {
      mentions.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
    }
  }

}


