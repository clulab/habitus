package org.clulab.habitus.utils

import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.struct.Interval

import scala.collection.immutable.ListMap


trait Context extends Attachment with Pairable

case class DefaultContext(publicationYear: String, location: String, country: String, date: String, process: String, crop: String, fertilizer: String, season:String, comparative: Int) extends Context

case class BeliefContext(context: String) extends Context

trait ContextExtractor {

  val NA = "N/A"
  val maxContextWindow = 2
  val processToLemmas = ListMap( // The order of keys is significant here.
    "planting"              -> Set("plant", "sow", "cultivate", "cultivation", "grow", "seed", "seeding", "seedling", "transplant", "cropping", "variety", "use", "growth"),
    "harvesting"            -> Set("harvest", "yield"),
    "credit"                -> Set("credit", "finance", "value"),
    "irrigation"            -> Set("irrigation", "irrigate"),
    "weeds"                 -> Set("weed"),
    "natural_disaster"      -> Set("flood", "bird", "attack", "floodwater"),
    "fertilizerApplication" -> Set("fertilizer", "application", "apply", "compost", "rate", "concentration"),
    "climate"               -> Set("climate"),
    "agriculture"           -> Set("agriculture"),
    "woodland"              -> Set("woodland", "woody", "wood"),
    "population"            -> Set("population", "occupy"),
    NA                      -> Set("N/A")
  )

  def reverseProcessToLemma(processToLemmas: ListMap[String, Set[String]]): Map[String, String] = {
    val countSeparate = processToLemmas.values.map(set => set.size).sum
    val countTogether = processToLemmas.values.flatten.size
    require(countSeparate == countTogether, "Lemmas should not (reverse) map to multiple processes!")

    processToLemmas
        .map { case (process, lemmas) => lemmas.map { lemma => lemma -> process } }
        .flatten
        .toMap
  }

  val lemmaToProcess: Map[String, String] = reverseProcessToLemma(processToLemmas)

  def getContextPerMention(mentions: Seq[Mention], doc: Document): Seq[Mention]

  def getProcess(mention: Mention): String = {
    val argLabels = mention.arguments.values.flatten.map(_.label).toSeq
    if (argLabels.contains("Fertilizer"))
      "fertilizerApplication"
    else {
      // Getting process lemma from the mention
      val mentionLemmas = mention.lemmas.get.toSet
      val mensLemmaOverLap = processToLemmas.map { case (process, lemmas) =>
        val intersectCount = lemmas.intersect(mentionLemmas).size
        process -> intersectCount
      }
      val max = mensLemmaOverLap.values.max
      if (max != 0)
        mensLemmaOverLap.filter(_._2 == max).keys.mkString("::")
      else {
        val closestLemma = findClosestProcessLemma(mention)
        lemmaToProcess(closestLemma)
      }
    }
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
            nextLoc.getOrElse(findClosestNotOverlapping(m, contextRelevantMentions)).text
          }
          case "Date" => findClosestNotOverlapping(m, contextRelevantMentions).text
          case "Crop" => if (m.foundBy.endsWith("splitIntoBinary")) {
            findClosestNotOverlapping(m, contextRelevantMentions).text
          } else findOverlappingOrClosestNotOverlapping(m, contextRelevantMentions).text

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

  def getDistance(mention: Mention, lemmaIdx: (String, Int)): Int = {
    val lemmaTokInt = Interval(lemmaIdx._2, lemmaIdx._2 + 1)
    val sorted = Seq(mention.tokenInterval, lemmaTokInt).sorted
    val dist = sorted.last.start - sorted.head.end
    dist
  }

  def findClosest(mention: Mention, mentions: Seq[Mention]): Mention = {
    mentions.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
  }

  def findClosestProcessLemma(mention: Mention): String = {
    val lemmasWithIndices = mention.sentenceObj.lemmas.get.zipWithIndex.filter(li => lemmaToProcess.contains(li._1))
    val lemmasWithDistances = lemmasWithIndices.map(tup => (tup._1, getDistance(mention, tup)))
    if (lemmasWithDistances.nonEmpty) {
      lemmasWithDistances.minBy(_._2)._1
    } else NA
  }


  def findClosestNotOverlapping(mention: Mention, mentions: Seq[Mention]): Mention = {
    // check if there are context mentions (e.g., date or crop) that don't overlap with the mention itself and if yes, pick closest of those
    // if there are no non-intersecting mentions, just pick any nearest one
    val nonIntersecting = mentions.filter(m => m.tokenInterval.intersect(mention.tokenInterval).isEmpty)
    if (nonIntersecting.nonEmpty) {
      nonIntersecting.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
    } else {
      mentions.map(m => (m, getDistance(mention, m))).minBy(_._2)._1
    }
  }
  def findOverlappingOrClosestNotOverlapping(mention: Mention, mentions: Seq[Mention]): Mention = {
    // check if there are context mentions (e.g., date or crop) that don't overlap with the mention itself and if yes, pick closest of those
    // if there are no non-intersecting mentions, just pick any nearest one
    val overlapping = mentions.filter(m => m.tokenInterval.intersect(mention.tokenInterval).nonEmpty)
    if (overlapping.nonEmpty) overlapping.head
    else findClosestNotOverlapping(mention, mentions)
  }
}
