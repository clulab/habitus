package org.clulab.habitus.utils

import org.clulab.habitus.variables.EntityDistFreq

import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.Document

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
    getArgValuePairs().map(_._2).mkString("\t")
  }
}

case class DefaultContext(location: String, date: String, crop: String, fertilizer: String, comparative: Int) extends Context

trait ContextExtractor {

  def getContextPerMention(mentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq], doc: Document, label: String): Seq[Mention]

  def getComparative(mention: Mention): Int = {
    val relative = Seq("vs", "vs.", "respectively")
    if (mention.sentenceObj.words.intersect(relative).nonEmpty) 1 else 0
  }

  def getLocation(m: Mention, thisSentLocs: Seq[Mention], frequencyContext: Map[Int, ContextDetails]): String = {
    val location = thisSentLocs.length match {
      // if no locations in sentence, use the most freq one in +/- 1 sent window
      case 0 => if (frequencyContext.nonEmpty) {
        frequencyContext(m.sentence).mostFreqLoc1Sent
      } else "N/A"
      case 1 => thisSentLocs.head.text
      case _ => {
        val nextLoc = findClosestNextLocation(m, thisSentLocs)
        if (nextLoc != null) nextLoc.text else "N/A"
      }
    }
    location
  }

  def getCropContext(m: Mention, frequencyContext: Map[Int, ContextDetails]): String = {
    if (frequencyContext.nonEmpty) {
      frequencyContext(m.sentence).mostFreqCrop0Sent
    } else "N/A"
  }

  def getFertilizerContext(m: Mention, frequencyContext: Map[Int, ContextDetails]): String = {
    if (frequencyContext.nonEmpty) {
      frequencyContext(m.sentence).mostFreqFertilizer1Sent
    } else "N/A"
  }

  def getDate(m: Mention, thisSentDates: Seq[Mention], frequencyContext: Map[Int, ContextDetails]): String = {
    // if no dates in sentence, use the most freq one in +/- 1 sent window
    val date = thisSentDates.length match {
      case 0 => if (frequencyContext.nonEmpty) {
        frequencyContext(m.sentence).mostFreqDate1Sent
      } else "N/A"// because there are no dates in this sentence, take most freq in -/+ 1 window
      case 1 => thisSentDates.head.text
      case _ => findClosestDate(m, thisSentDates).text
    }
    date
  }

  def compressContext(doc: Document, allEventMentions: Seq[Mention], entityHistogram: Seq[EntityDistFreq]): mutable.Map[Int, ContextDetails] = {
    //sentidContext is a data structure created just to carry contextdetails to the code which writes output to disk
    //note: value=Seq[contextDetails] because there can be more than one mentions in same sentence
    val sentidContext = mutable.Map[Int, ContextDetails]()

    // for each of the event mentions, find most frequent entityType within the distance of howManySentAway
    //  e.g.,(LOC,1) means find which Location occurs most frequently within 1 sentence of this event
    val mostFreqLocation0Sent = extractContext(doc, allEventMentions, 0, "LOC", entityHistogram)
    val mostFreqLocation1Sent = extractContext(doc, allEventMentions, 1, "LOC", entityHistogram)
    val mostFreqLocationOverall = extractContext(doc, allEventMentions, Int.MaxValue, "LOC", entityHistogram)
    val mostFreqDate0Sent = extractContext(doc, allEventMentions, 0, "DATE", entityHistogram)
    val mostFreqDate1Sent = extractContext(doc, allEventMentions, 1, "DATE", entityHistogram)
    val mostFreqDateOverall = extractContext(doc, allEventMentions, Int.MaxValue, "DATE", entityHistogram)
    val mostFreqCrop0Sent = extractContext(doc, allEventMentions, 0, "CROP", entityHistogram)
    val mostFreqCrop1Sent = extractContext(doc, allEventMentions, 1, "CROP", entityHistogram)
    val mostFreqCropOverall = extractContext(doc, allEventMentions, Int.MaxValue, "CROP", entityHistogram)
    val mostFreqFertilizer0Sent = extractContext(doc, allEventMentions, 0, "FERTILIZER", entityHistogram)
    val mostFreqFertilizer1Sent = extractContext(doc, allEventMentions, 1, "FERTILIZER", entityHistogram)
    val mostFreqFertilizerOverall = extractContext(doc, allEventMentions, Int.MaxValue, "FERTILIZER", entityHistogram)

    //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
    createSentidContext(sentidContext, mostFreqLocation0Sent, mostFreqLocation1Sent, mostFreqLocationOverall,
      mostFreqDate0Sent, mostFreqDate1Sent, mostFreqDateOverall, mostFreqCrop0Sent, mostFreqCrop1Sent,
      mostFreqCropOverall,mostFreqFertilizer0Sent, mostFreqFertilizer1Sent,
      mostFreqFertilizerOverall)

    sentidContext
  }

  def findMostFreqContextEntitiesForAllEvents(mentionContextMap: mutable.Map[Mention, Seq[EntityDistFreq]], howManySentAway:Int, entityType:String):Seq[MostFreqEntity] = {
    mentionContextMap.keys.toSeq.map(key=>findMostFreqContextEntitiesForOneEvent(key,mentionContextMap(key), entityType,howManySentAway))
  }

  //if key exists add+1 to its value, else add 1 as its value
  def checkAddFreq(map: mutable.Map[String, Int], key: String, freq: Int): Int = {
    val newFreq = map.getOrElse(key, 0) + freq

    map(key) = newFreq
    newFreq
  }

  //given a user input (e.g.,LOC,1-- which means find which Location occurs most frequently within 1 sentence of this
  // event), calculate it from available frequency and sentence data
  def findMostFreqContextEntitiesForOneEvent(mention:Mention, contexts:Seq[EntityDistFreq], entityType:String, howManySentAway:Int):MostFreqEntity= {
    val entityFreq = mutable.Map[String, Int]()
    var maxFreq = 0
    var mostFreqEntity = ""
    //go through each of the contexts and find if any of them satisfies the condition in the query
    for (ctxt <- contexts) {
      for (sentFreq <- ctxt.entityDistFrequencies) {
        val sentDist = sentFreq._1
        val freq = sentFreq._2
        if (sentDist <= howManySentAway && ctxt.nerTag.contains(entityType)) {
          val newFreq = checkAddFreq(entityFreq, ctxt.entityValue, freq)
          if (newFreq >= maxFreq) {
            maxFreq = newFreq
            mostFreqEntity = ctxt.entityValue
          }
        }
      }
    }
    MostFreqEntity(mention.sentence, mention.words.mkString(" "), checkIfNoName(mostFreqEntity))
  }

  def checkIfNoName(s: String): Option[String] = if (s.isEmpty) None else Some(s)

  //note: output of extractContext is a sequence of MostFreqEntity (sentId,mention, mostFreqEntity)) case classes.
  // It is a sequence because there can be more than one eventmentions that can occur in the given document
  def extractContext(doc: Document, allEventMentions:Seq[Mention], howManySentAway:Int,
                     entityType:String, entityHistogram:Seq[EntityDistFreq] ):Seq[MostFreqEntity]= {
    //compressing part: for each mention find the entity that occurs within n sentences from it.
    val mentionContextMap = getEntityRelDistFromMention(allEventMentions, entityHistogram)


    // For the given query, return answer. where-
    //answer=MostFreqEntity=(sentId: Int, mention: String, mostFreqEntity: String)
    // i.e  for the given query :(eventmention mention, which occurs in sentence no: sentId,)
    // the most frequent entity within a distance of n is mostFreqEntity
    findMostFreqContextEntitiesForAllEvents(mentionContextMap, howManySentAway, entityType)
  }

  //for each mention find how far away an entity occurs, and no of times it occurs in that sentence
  def getEntityRelDistFromMention(mentionsSentIds: Seq[Mention], contexts:Seq[EntityDistFreq]): mutable.Map[Mention, Seq[EntityDistFreq]]= {
    val mentionsContexts = mutable.Map[Mention, Seq[EntityDistFreq]]()
    for (mention <- mentionsSentIds) {
      val contextsPerMention = new ArrayBuffer[EntityDistFreq]()
      for (context <- contexts) {
        val relDistFreq = ArrayBuffer[(Int,Int)]()
        for (absDistFreq <- context.entityDistFrequencies) {
          //first value of tuple absDistFreq is abs distance. use it to calculate relative distance to this mention
          val relDistance = (mention.sentence - absDistFreq._1).abs
          //second value of the tuple absDistFreq is the freq: how often does that entity occur in this sentence
          val freq = absDistFreq._2
          relDistFreq.append((relDistance, freq))
          //context.entityDistFrequencies=relDistFreq

        }
        //same entityAbsDistFreq case class is being reused here. But diff is we store relative distance now instead of absolute
        val ctxt=context.copy(entityDistFrequencies=relDistFreq)
        contextsPerMention += ctxt
      }

      //create a map between each mention and its corresponding sequence. this will be useful in the reduce/compression part
      mentionsContexts += (mention -> contextsPerMention)
    }
    mentionsContexts
  }

  def checkSentIdContextDetails(sentidContext: mutable.Map[Int,ContextDetails], key:Int, value: ContextDetails): Unit = {
    sentidContext.get(key) match {
      case Some(_) =>
      //        println(s"Found that multiple event mentions occur in the same sentence with sentence id $key. " +
      //          s"going to add to chain of values")
      //        val oldList=sentidContext(key)
      //        oldList.append(value)
      //        sentidContext(key) = oldList
      case None =>
        //the combination of (sentid,freq) becomes the key for the value
        sentidContext(key) = value
    }
  }
  case class MostFreqEntity(sentId: Int, mention: String, mostFreqEntity: Option[String])


  def createSentidContext(sentidContext: mutable.Map[Int,ContextDetails],
                          mostFreqLocation0Sent:Seq[MostFreqEntity],
                          mostFreqLocation1Sent:Seq[MostFreqEntity],
                          mostFreqLocationOverall:Seq[MostFreqEntity],
                          mostFreqDate0Sent:Seq[MostFreqEntity],
                          mostFreqDate1Sent:Seq[MostFreqEntity],
                          mostFreqDateOverall:Seq[MostFreqEntity],
                          mostFreqCrop0Sent:Seq[MostFreqEntity],
                          mostFreqCrop1Sent:Seq[MostFreqEntity],
                          mostFreqCropOverall:Seq[MostFreqEntity],
                          mostFreqFertilizer0Sent:Seq[MostFreqEntity],
                          mostFreqFertilizer1Sent:Seq[MostFreqEntity],
                          mostFreqFertilizerOverall:Seq[MostFreqEntity]
                         ): Unit= {

    //todo assert lengths of all the mostFreq* are same

    //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
    //note: zipping through only the list of one mostFreq* since all of them should have same lenghts.
    for ((mostFreq, i) <- mostFreqLocation0Sent.zipWithIndex) {
      checkSentIdContextDetails(sentidContext,mostFreq.sentId,
        ContextDetails(mostFreq.mention,
          checkIfEmpty(mostFreq.mostFreqEntity),
          checkIfEmpty(mostFreqLocation1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqLocationOverall(i).mostFreqEntity),
          checkIfEmpty(mostFreqDate0Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqDate1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqDateOverall(i).mostFreqEntity),
          checkIfEmpty(mostFreqCrop0Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqCrop1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqCropOverall(i).mostFreqEntity),
          checkIfEmpty(mostFreqFertilizer0Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqFertilizer1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqFertilizerOverall(i).mostFreqEntity)
        )
      )
    }
  }
  //if none, return "N/A", else return String value of entity e.g.:"SENEGAL"
  def checkIfEmpty(mostFreqEntity: Option[String]): String = mostFreqEntity.getOrElse("N/A")

  def findClosestNextLocation(mention: Mention, locations: Seq[Mention]): Mention = {
    if (locations.length == 1) return locations.head
    val nextLocations = locations.filter(_.tokenInterval.start > mention.arguments("value").head.tokenInterval.start)
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


