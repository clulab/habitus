package org.clulab.variables

import org.clulab.dynet.Utils
import org.clulab.odin.{EventMention, ExtractorEngine, Mention, TextBoundMention}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Document, Processor}
import org.clulab.sequences.LexiconNER
import org.clulab.utils.contextDetails
import scala.collection.mutable.{ArrayBuffer, Map}
import scala.util.Try
import scala.util.control.Breaks._
import org.clulab.processors.Sentence

class ContextExtractor(val processor: Processor, val extractor: ExtractorEngine) {


  def parse(doc:Document,mentions:Seq[Mention],sentidContext:scala.collection.mutable.Map[Int,contextDetails]) = {

    //output of extractContext is a sequence of MostFreqEntity (sentId,mention, mostFreqEntity)) case classes.
    // It is a sequence because there can be more than one eventmentions that can occur in the given document

    //todo: load LOC, DATE etc from a list provided by user- once we finalize what all is needed
    //todo: load 0,1,all from a list provided by user- once we finalize which all we need
    val mostFreqLocation0Sent=extractContext(doc,mentions,0,"LOC")
    val mostFreqLocation1Sent=extractContext(doc,mentions,1,"LOC")
    val mostFreqLocationOverall=extractContext(doc,mentions,Int.MaxValue,"LOC")
    val mostFreqDate0Sent=extractContext(doc,mentions,0,"DATE")
    val mostFreqDate1Sent=extractContext(doc,mentions,1,"DATE")
    val mostFreqDATEOverall=extractContext(doc,mentions,Int.MaxValue,"DATE")


    //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
    createSentidContext(sentidContext,mostFreqLocation0Sent,mostFreqLocation1Sent,mostFreqLocationOverall,mostFreqDate0Sent,mostFreqDate1Sent,mostFreqDATEOverall)

  }

  def checkSentIdContextDetails(sentidContext:scala.collection.mutable.Map[Int,contextDetails], key:Int,value: contextDetails) = {
    sentidContext.get(key) match {
      case Some(value) =>
        println("Error. repeated key")
      case None => sentidContext(key) = value
    }

  }
  def createSentidContext(sentidContext:scala.collection.mutable.Map[Int,contextDetails],
                          mostFreqLocation0Sent:Seq[MostFreqEntity],
                          mostFreqLocation1Sent:Seq[MostFreqEntity],
                          mostFreqLocation:Seq[MostFreqEntity],
                          mostFreqDate0Sent:Seq[MostFreqEntity],
                          mostFreqDate1Sent:Seq[MostFreqEntity],
                          mostFreqDate:Seq[MostFreqEntity]): Unit= {

    //todo assert lengths of all the mostFreq* are same
    //for each event mention, get the sentence id, and map it to a case class called contextDetails, which will have all of mostFreq* information
    //zipping through only the list of one mostFreq* since all of them should have same lenghts
    for ((mostFreq, i) <- (mostFreqLocation0Sent).zipWithIndex) {
      checkSentIdContextDetails(sentidContext,mostFreq.sentId, contextDetails(mostFreq.mention, mostFreq.mostFreqEntity,
        mostFreqLocation1Sent(i).mostFreqEntity, mostFreqLocation(i).mostFreqEntity,mostFreqDate0Sent(i).mostFreqEntity,
        mostFreqDate1Sent(i).mostFreqEntity, mostFreqDate(i).mostFreqEntity))
    }
  }

  def extractContext(doc: Document, mentions:Seq[Mention], n:Int, entityType:String ):Seq[MostFreqEntity]= {
    //collect all event entities only (and not text bound ones)
    val allEventMentions = mentions.collect { case m: EventMention => m }

    //get sequence of Entity (refer  case class Entity)
    val allContexts = getEntityFreqPerSent(doc)

    //for each mention find the entity that occurs within n sentences from it.
    val mentionContextMap = getEntityRelDistFromMention(allEventMentions, allContexts)

    //pick entityType from ["LOC","DATE"]; set n=Int.MaxValue to get overall context/frequency in whole document
    findMostFreqContextEntitiesForAllEvents(mentionContextMap, n, entityType)
  }

  //for each mention find how far away an entity occurs, and no of times it occurs in that sentence
 def getEntityRelDistFromMention(mentionsSentIds: Seq[EventMention], contexts:Seq[entityRelDist])= {
   val mentionsContexts=  scala.collection.mutable.Map[EventMention, Seq[entityRelDist]]()
   for (mention <- mentionsSentIds) {
     var contextsPerMention = new ArrayBuffer[entityRelDist]()
     for (x <- contexts) {
       val relDistFreq = ArrayBuffer[Array[Int]]()
       for (y <- x.relDistFreqOfEntity) {
         //take the absolute value of entities in context, and calculate relative distance to this mention
         val relDistance = (mention.sentence - y(0)).abs
         val freq = y(1)
         relDistFreq += Array(relDistance, freq)
       }
       val ctxt = new entityRelDist(x.entityValue, x.nerTag, relDistFreq)
       contextsPerMention += ctxt
     }
     mentionsContexts += (mention -> (contextsPerMention.toSeq))
   }
   mentionsContexts
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
  case class MostFreqEntity(sentId: Int, mention: String, mostFreqEntity: String)

  def findMostFreqContextEntitiesForOneEvent(mention:EventMention, contexts:Seq[entityRelDist], entityType:String, n:Int):MostFreqEntity= {
    var entityFreq = scala.collection.mutable.Map[String, Int]()
    var maxFreq = 0
    var mostFreqEntity = ""
    for (ctxt <- contexts) {
      for (sentFreq <- ctxt.relDistFreqOfEntity) {
        val sentDist = sentFreq(0)
        val freq = sentFreq(1)
        if (sentDist <= n && ctxt.nerTag.contains(entityType)) {
          entityFreq = checkAddFreq(entityFreq, ctxt.entityValue, freq)
          if (entityFreq(ctxt.entityValue) >= maxFreq) {
            maxFreq = entityFreq(ctxt.entityValue)
            mostFreqEntity = ctxt.entityValue
          }
        }
      }
    }
    MostFreqEntity(mention.sentence, mention.words.mkString(" "), mostFreqEntity)
  }
  //for each event, find most the frequent X entity (X can be Location , Year etc) within n sentences. note: there can be multiple event mentions per sentence
  def findMostFreqContextEntitiesForAllEvents(mentionContextMap: scala.collection.mutable.Map[EventMention, Seq[entityRelDist]], n:Int, entityType:String):Seq[MostFreqEntity] = {
    mentionContextMap.keys.toSeq.map(key=>findMostFreqContextEntitiesForOneEvent(key,mentionContextMap(key), entityType,n))
  }


  // a case class to hold the entity value and its ner tag together
  case class entityNameEntity(entityValue: String, nerTag: String)

  //for each entity, find which sentence they occur in an how many times
  def mapEntityToFreq(entitySentFreq: scala.collection.mutable.Map[Entity, Int]):Seq[entityRelDist]  = {
    val sentIdFreq= scala.collection.mutable.Map[entityNameEntity, ArrayBuffer[Array[Int]]]()
    for (key <- entitySentFreq.keys) {
      val entityName = key.entityValue
      val entity = key.tag
      val sentId = key.sentIdx
      val freq = entitySentFreq(key)
      val nk = entityNameEntity(entityName, entity)
      //if the entity_sentenceid combination already exists in the dictionary, increase its frequency by 1, else add.
      sentIdFreq.get(nk) match {
        case Some(i) =>
          var freqNew = sentIdFreq(nk)
          var sentfreqa = Array(sentId, freq)
          freqNew += sentfreqa
          sentIdFreq += (nk -> freqNew)
        case None =>
          val sentfreq = ArrayBuffer[Array[Int]]()
          sentfreq += Array(sentId, freq)
          sentIdFreq += (nk -> sentfreq)
      }
    }
    convertMapToSeq(sentIdFreq)
  }

  //if key exists add+1 to its value, else add 1 as its value
  def checkIncreaseFreq(mapper: scala.collection.mutable.Map[Entity, Int], key: Entity):Unit= {
    mapper.get(key) match {
      case Some(value) =>
        mapper(key) = value+1
      case None => mapper(key) = 1
    }

  }


  def convertMapToSeq(sentIdFreq: scala.collection.mutable.Map[entityNameEntity, ArrayBuffer[Array[Int]]])=
  {
    var contexts = new ArrayBuffer[entityRelDist]()
    for (key <- sentIdFreq.keys) {
      contexts += new entityRelDist(key.entityValue, key.nerTag,sentIdFreq(key))
    }
    contexts.toSeq
  }

  //details of each entity: name,nertag, index of sentence it was found in
  case class Entity(entityValue: String, tag: String, sentIdx: Int)

  // IF an LOC entity has multiple tokens. (e.g., United States of America.) merge them to form one entityName
  def checkForMultipleTokens(nerTag:String,entityCounter:Int,indicesToSkip:ArrayBuffer[Int],entityName:String,sent:Sentence): String = {
    var newEntityName = entityName
    var fullName = ArrayBuffer[String]()
    var tempEntity = nerTag
    var tempCounter = entityCounter
    breakable {
      do {
        fullName += sent.words(tempCounter)
        indicesToSkip += tempCounter
        tempCounter = tempCounter + 1
        if (tempCounter < sent.words.length) {
          tempEntity = sent.entities.get(tempCounter)
        }
        else
          {break()}
        newEntityName = fullName.mkString(" ").toLowerCase()
      } while (tempEntity == "I-LOC")
    }
    newEntityName
  }

  //For each entity find which sentence it occurs in and its overall frequency
  def getEntityFreqPerSent(doc: Document): Seq[entityRelDist] = {
    var entitySentFreq: scala.collection.mutable.Map[Entity, Int] = Map()
    for ((s, i) <- doc.sentences.zipWithIndex) {
      var entityCounter = 0
      val indicesToSkip = ArrayBuffer[Int]()
      for ((nerTag, word, norm) <- (s.entities.get, s.words, s.norms.get).zipped) {
        var EntityNameIndex: Option[Entity] = None
        if (!indicesToSkip.contains(entityCounter)) {
          var namedEntityTag = ""
          var entityName = word.toLowerCase
          if (nerTag == "B-LOC") {
            val cleanNerTag = "LOC"
            val newEntityName = checkForMultipleTokens(nerTag, entityCounter, indicesToSkip, entityName, s)
            EntityNameIndex = Some(Entity(newEntityName, cleanNerTag, i))
          }
          else if (nerTag.contains("B-DATE")) {
            namedEntityTag = "DATE"
            val split0 = norm.split('-').head
            if (split0 != "XXXX" && Try(split0.toInt).isSuccess) {
              entityName = split0
              EntityNameIndex = Some(Entity(entityName, namedEntityTag, i))
            }
          }
          if (EntityNameIndex.isDefined) {
            //check if this entity was seen in this sentence before. if yes increase its frequency
            checkIncreaseFreq(entitySentFreq, EntityNameIndex.get)
          }
        }
        entityCounter = entityCounter + 1
      }
    }
    mapEntityToFreq(entitySentFreq)
  }
}

object ContextExtractor {
  def apply(): ContextExtractor = {
    // Custom NER for variable reading
    val kbs = Seq(
      "variables/FERTILIZER.tsv"
    )
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true // case insensitive match for fertilizers
      )
    )

    // create the processor
    Utils.initializeDyNet()
    val processor: Processor = new CluProcessor(optionalNER = Some(lexiconNer))

    // read rules from yml file in resources
    val source = io.Source.fromURL(getClass.getResource("/variables/master.yml"))
    val rules = source.mkString
    source.close()

    // creates an extractor engine using the rules and the default actions
    val extractor = ExtractorEngine(rules)

    new ContextExtractor(processor, extractor)
  }
}

//For each entity what is relDistFreqOfEntity
//relDistFreqOfEntity= [int a,int b], where a=relative distance from the nearest mention
// and b=no of times that context word occurs in that sentence. e.g,: Senegal=[0,3]- means Senegal occurs thrice in sentence 0
class entityRelDist(var entityValue: String, var nerTag: String, val relDistFreqOfEntity:ArrayBuffer[Array[Int]])
