package org.clulab.variables
import scala.util.control.NonFatal
import org.clulab.habitus.Main.mentions
import org.clulab.odin.{EventMention, Mention}
import org.clulab.processors.Document
import org.clulab.utils.{FileUtils, StringUtils, contextDetails, outputMentionsToTSV}

import java.io._
import scala.collection.mutable.ArrayBuffer

object VariableReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToProperties(args)

    val inputDir = props.getProperty("in")
    assert(inputDir != null)
    val outputDir = props.getProperty("out")
    assert(outputDir != null)
    val output = new File(outputDir).mkdir()
    val vp = VariableProcessor()

    var seqMention = Seq[String]()
    var outputFile = outputDir + "/mentions.tsv"


    val pw = new PrintWriter(new FileWriter(new File(outputFile)))
    for (file <- FileUtils.findFiles(inputDir, ".txt")) {
      try {
        val text = FileUtils.getTextFromFile(file)
        val filename = file.toString.split("/").last
        println(s"going to parse input file: $filename")
        val (doc, mentions, allEventMentions, entityHistogram) = vp.parse(text)
        val context = compressContext(doc, allEventMentions, entityHistogram)
        println(s"Writing mentions from doc ${filename} to $outputFile")
        outputMentionsToTSV(mentions, doc, context, filename, pw)
        // to not overpopulate the memory. Flush findings once for each document.
        pw.flush()
      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
    pw.close()
  }


    def compressContext(doc:Document, allEventMentions:Seq[EventMention], entityHistogram:Seq[EntityDistFreq]) = {

      //sentidContext is a data structure created just to carry contextdetails to the code which writes output to disk
      //note: value=Seq[contextDetails] because there can be more than one mentions in same sentence
      val sentidContext = scala.collection.mutable.Map[Int, ArrayBuffer[contextDetails]]()

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

      //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
      createSentidContext(sentidContext, mostFreqLocation0Sent, mostFreqLocation1Sent, mostFreqLocationOverall,
        mostFreqDate0Sent, mostFreqDate1Sent, mostFreqDateOverall, mostFreqCrop0Sent, mostFreqCrop1Sent, mostFreqCropOverall)

      sentidContext
    }

  def findMostFreqContextEntitiesForAllEvents(mentionContextMap: scala.collection.mutable.Map[EventMention, Seq[EntityDistFreq]], howManySentAway:Int, entityType:String):Seq[MostFreqEntity] = {
    mentionContextMap.keys.toSeq.map(key=>findMostFreqContextEntitiesForOneEvent(key,mentionContextMap(key), entityType,howManySentAway))
  }


  //if key exists add+1 to its value, else add 1 as its value
  def checkAddFreq(mapper: scala.collection.mutable.Map[String, Int], key: String, freq:Int): scala.collection.mutable.Map[String, Int] = {
    mapper.get(key) match {
      case Some(value) =>
        mapper(key) = value+freq
      case None => mapper(key) = freq
    }
    mapper
  }

  //given a user input (e.g.,LOC,1-- which means find which Location occurs most frequently within 1 sentence of this
  // event), calculate it from available frequency and sentence data
  def findMostFreqContextEntitiesForOneEvent(mention:EventMention, contexts:Seq[EntityDistFreq], entityType:String, howManySentAway:Int):MostFreqEntity= {
    var entityFreq = scala.collection.mutable.Map[String, Int]()
    var maxFreq = 0
    var mostFreqEntity = ""
    //go through each of the contexts and find if any of them satisfies the condition in the query
    for (ctxt <- contexts) {
      for (sentFreq <- ctxt.entityDistFrequencies) {
        val sentDist = sentFreq._1
        val freq = sentFreq._2
        if (sentDist <= howManySentAway && ctxt.nerTag.contains(entityType)) {
          entityFreq = checkAddFreq(entityFreq, ctxt.entityValue, freq)
          if (entityFreq(ctxt.entityValue) >= maxFreq) {
            maxFreq = entityFreq(ctxt.entityValue)
            mostFreqEntity = ctxt.entityValue
          }
        }
      }
    }
    MostFreqEntity(mention.sentence, mention.words.mkString(" "), checkIfNoName(mostFreqEntity))
  }


  def checkIfNoName(s:String):Option[String]=
  {
    if (s==""){
      return None
    }
    else
    {
      return Some(s)
    }
  }

  //note: output of extractContext is a sequence of MostFreqEntity (sentId,mention, mostFreqEntity)) case classes.
  // It is a sequence because there can be more than one eventmentions that can occur in the given document
  def extractContext(doc: Document, allEventMentions:Seq[EventMention], howManySentAway:Int,
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
  def getEntityRelDistFromMention(mentionsSentIds: Seq[EventMention], contexts:Seq[EntityDistFreq]):scala.collection.mutable.Map[EventMention, Seq[EntityDistFreq]]= {
    val mentionsContexts=  scala.collection.mutable.Map[EventMention, Seq[EntityDistFreq]]()
    for (mention <- mentionsSentIds) {
      var contextsPerMention = new ArrayBuffer[EntityDistFreq]()
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
      mentionsContexts += (mention -> (contextsPerMention.toSeq))
    }
    mentionsContexts
  }

  def checkSentIdContextDetails(sentidContext:scala.collection.mutable.Map[Int,ArrayBuffer[contextDetails]], key:Int, value: contextDetails) = {
    sentidContext.get(key) match {
      case Some(i) =>
        println(s"Found that multiple event mentions occur in the same sentence with sentence id {key}. " +
          s"going to add to chain of values")
        val oldList=sentidContext(key)
        oldList.append(value)
        sentidContext(key) = oldList
      case None =>
        //the combination of (sentid,freq) becomes the key for the value
        sentidContext(key) = ArrayBuffer(value)
    }
  }
  case class MostFreqEntity(sentId: Int, mention: String, mostFreqEntity: Option[String])


  def createSentidContext(sentidContext:scala.collection.mutable.Map[Int,ArrayBuffer[contextDetails]],
                          mostFreqLocation0Sent:Seq[MostFreqEntity],
                          mostFreqLocation1Sent:Seq[MostFreqEntity],
                          mostFreqLocationOverall:Seq[MostFreqEntity],
                          mostFreqDate0Sent:Seq[MostFreqEntity],
                          mostFreqDate1Sent:Seq[MostFreqEntity],
                          mostFreqDateOverall:Seq[MostFreqEntity],
                          mostFreqCrop0Sent:Seq[MostFreqEntity],
                          mostFreqCrop1Sent:Seq[MostFreqEntity],
                          mostFreqCropOverall:Seq[MostFreqEntity]): Unit= {

    //todo assert lengths of all the mostFreq* are same

    //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
    //note: zipping through only the list of one mostFreq* since all of them should have same lenghts.
    for ((mostFreq, i) <- (mostFreqLocation0Sent).zipWithIndex) {
      checkSentIdContextDetails(sentidContext,mostFreq.sentId,
        contextDetails(mostFreq.mention,
          checkIfEmpty(mostFreq.mostFreqEntity),
          checkIfEmpty(mostFreqLocation1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqLocationOverall(i).mostFreqEntity),
          checkIfEmpty(mostFreqDate0Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqDate1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqDateOverall(i).mostFreqEntity),
          checkIfEmpty(mostFreqCrop0Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqCrop1Sent(i).mostFreqEntity),
          checkIfEmpty(mostFreqCropOverall(i).mostFreqEntity)
        )
      )
    }
  }
  //if none, return "", else return String value of entity e.g.:"SENEGAL"
  def checkIfEmpty(mostFreqEntity:Option[String]): String =
  {
    mostFreqEntity match
    {
      case Some(i)=> return mostFreqEntity.get
      case None => return "N/A"
    }
  }
}
