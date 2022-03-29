package org.clulab.habitus.utils

//import org.clulab.factuality.Factuality
import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.{Document, Sentence}
import org.clulab.struct.Interval
//import org.clulab.factuality.Factuality
import scala.util.control.Breaks._

import scala.collection.{breakOut, mutable}
import scala.collection.mutable.ArrayBuffer

class Factuality(path: String) {

  def predict(words: Array[String], pos: Int): Float = 0.5f
}

object Factuality {
  def apply(path: String): Factuality = new Factuality(path)
}

trait Context extends Attachment {
  def getArgValuePairs() = this.getClass.getDeclaredFields.toList
    .map(arg => {
      arg.setAccessible(true)
      (arg.getName, arg.get(this))
    })

  def getTSVContextHeader() = {
    getArgValuePairs().map(_._1).mkString("\t")
  }

  def getTSVContextString() = {
    getArgValuePairs().map(_._2).map { value =>
      value match {
        case None => ""
        case Some(x) => x.toString
        case x => x.toString
      }
    }
    .mkString("\t")
  }
}

// Some of these variables are in Python format because they get serialized to JSON automatically.
case class DefaultContext(location: String, date: String, process: String, crop: String, fertilizer: String, comparative: Int,
    factuality_score: Option[Float], factuality_verb: Option[String]) extends Context

trait ContextExtractor {

  val NA = "N/A"
  val maxContextWindow = 2

  val plantingLemmas = Seq("plant", "sow", "cover", "cultivate", "grow")
  val creditLemmas = Seq("credit", "finance", "value", "correspond")
  val harvestLemmas = Seq("harvest")
  val disasterLemmas = Seq("flood", "bird", "attack")

  def getContextPerMention(mentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq], doc: Document, label: String): Seq[Mention]

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
    } else if (lemmas.exists(l => disasterLemmas.contains(l))) {
      "natural_disaster"
    } else "planting" // for now assume anything else is planting //sentVerbs.filter(w => !stopVerbs.contains(w)).mkString("::") // for checking verbal triggers
    process
  }

  def getComparative(mention: Mention): Int = {
    val relative = Seq("vs", "vs.", "respectively")
    if (relative.exists(mention.sentenceObj.words.contains(_))) 1 else 0
  }


  def getFactualityScore(m: Mention): Option[(Float,String)] = {
    for ((postags) <- m.tags) {
      for ((tag, i) <- postags.zipWithIndex) {
        if (tag.contains("VB")) {
          if (!m.words(i).equals("vs") && (!m.lemmas.get(i).equals("be")) && (!m.lemmas.get(i).equals("have"))) {
            val token = m.words(i)
            val factuality = Factuality("org/clulab/factuality/models/FTrainFDevScim3")
            val factualityScore = factuality.predict(m.words.toArray, i)
            //              assert(factualityScore.get >= 0.0)
            //              assert(predicateIndex >= 0.0)
            return Some((factualityScore, token))
          }
        }
      }
    }
    None
    // if you get none here, i.e there are no verbs in the mention, do a +-  window for(m.sentences.word start-5 and end +5 - math. min or max/past or end of
    // sentence. if sentences has less than +k) check factulaity score.

  }

  def getContext(m: Mention, contextType: String, contextRelevantMentions: Seq[Mention], allMentions: Seq[Mention]): String = {
    // if no relevant context mentions in sentence, use the most freq one in sentence window equal to +/- maxContextWindow
    val context = contextRelevantMentions.length match {
      case 0 => getMostFrequentInContext(m, contextType, maxContextWindow, allMentions)
      case 1 => contextRelevantMentions.head.text.toLowerCase()
      case _ => {
        contextType match {
          case "Location" => {
            val nextLoc = findClosestNextLocation(m, contextRelevantMentions)
            if (nextLoc.isDefined) nextLoc.get.text else NA
          }
          case _ => findClosest(m, contextRelevantMentions).text.toLowerCase()
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
        return instances.groupBy(identity).map(i => i._1 -> i._2.length).max._1.toLowerCase()
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


