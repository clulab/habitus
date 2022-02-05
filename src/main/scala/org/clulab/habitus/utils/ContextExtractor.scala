package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.Document

import scala.collection.mutable.ArrayBuffer

case class Context(location: String, date: String) extends Attachment {
  def getArgs() = this.getClass.getDeclaredFields.toList
    .map(i => {
      i.setAccessible(true)
      i.getName -> i.get(this)
    }).toMap
}



class ContextExtractor {
  // todo: can get basic/shared context but should also be able to add type-specific context?

  def getContextPerMention(mentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq], doc: Document, label: String): Seq[Mention] = {
    val toReturn = new ArrayBuffer[Mention]()
    val (locMentions, nonLocs) = mentions.partition(_.label matches "Location")
    val (dateMentions, nonDates) = nonLocs.partition(_.label matches "Date")
    val mentionsBySentence = nonDates groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      val thisSentMentions = mentionsBySentence(i).distinct
      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val contentMentions = thisSentMentions.filter(_.label matches label)

      for (m <- contentMentions) {
        val date = dateMentions.length match {
          case 0 => "N/A"
          case 1 => dateMentions.head.text
          case _ => findClosestDate(m, dateMentions).text
        }
        val location = locMentions.length match {
          case 0 => "N/A"
          case _ => {
            val nextLoc = findClosestNextLocation(m, locMentions)
            if (nextLoc != null) nextLoc.text else "N/A"
          }
        }
        val context = new Context(date, location)
        val withAtt = m.withAttachment(context)
        toReturn.append(withAtt)
      }


    }
    toReturn

  }


  def findClosestNextLocation(mention: Mention, locations: Seq[Mention]): Mention = {
    if (locations.length == 1) return locations.head
    val nextLocations = locations.filter(_.tokenInterval.start > mention.arguments("value").head.tokenInterval.start)
    println("Sent: " + mention.sentenceObj.getSentenceText + "<<<")
    println("Men: " + mention.arguments("value").head.text)
    println("All locations: " + locations.map(_.text).mkString("||"))
    println("After locations: " + nextLocations.map(_.text).mkString("||"))
    if (nextLocations.nonEmpty) nextLocations.minBy(_.tokenInterval)
    else null
  }

  def findClosestDate(mention: Mention, dates: Seq[Mention]): Mention = {
    var minDist = 100
    var minDistDate = dates.head
    for (date <- dates) {
      if (math.abs(mention.tokenInterval.start - date.tokenInterval.end ) < minDist | math.abs(mention.tokenInterval.end - date.tokenInterval.start) < minDist) minDistDate = date
    }
    minDistDate
  }

}
