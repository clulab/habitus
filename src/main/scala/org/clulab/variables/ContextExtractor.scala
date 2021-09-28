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

class ContextExtractor(val processor: Processor, val extractor: ExtractorEngine) {


  def parse(text: String,sentidContext:scala.collection.mutable.Map[Int,contextDetails]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))


    val mostFreqLocation0Sent=extractContext(doc,mentions,0,"LOC")
    val mostFreqLocation1Sent=extractContext(doc,mentions,1,"LOC")
    val mostFreqLocation=extractContext(doc,mentions,Int.MaxValue,"LOC")
    val mostFreqDate0Sent=extractContext(doc,mentions,0,"DATE")
    val mostFreqDate1Sent=extractContext(doc,mentions,1,"DATE")
    val mostFreqDATE=extractContext(doc,mentions,Int.MaxValue,"DATE")

    createSentidContext(sentidContext,mostFreqLocation0Sent,mostFreqLocation1Sent,mostFreqLocation,mostFreqDate0Sent,mostFreqDate1Sent,mostFreqDATE)

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

    for ((zeroloc, i) <- (mostFreqLocation0Sent).zipWithIndex) {
      checkSentIdContextDetails(sentidContext,zeroloc.sentId, contextDetails(zeroloc.mention, zeroloc.mostFreqEntity,
        mostFreqLocation1Sent(i).mostFreqEntity, mostFreqLocation(i).mostFreqEntity,mostFreqDate0Sent(i).mostFreqEntity,
        mostFreqDate1Sent(i).mostFreqEntity, mostFreqDate(i).mostFreqEntity))
    }
  }

  def extractContext(doc: Document, mentions:Seq[Mention], n:Int, entity_type:String ):Seq[MostFreqEntity]={
    //map each event mention to its sentence id. useful in extracting contexts

    val allEventMentions = mentions.collect { case m: EventMention => m }

    // allContexts= map of all context entities (e.g.,Senegal) to the ids of sentences that they occur at
    val allContexts = extractContext(doc)
    val mentionContextMap=calculateAbsoluteDistance(allEventMentions,allContexts)

    //pick entityType from ["LOC","DATE"]; set n=Int.MaxValue to get overall context/frequency in whole document
    findMostFreqContextEntitiesForAllEvents(mentionContextMap,n, entity_type)
  }

  //map each mention to and how far an entity occurs, and no of times it occurs in that sentence

 def calculateAbsoluteDistance(mentionsSentIds: Seq[EventMention], contexts:Seq[Context])= {
   val mentionsContexts=  scala.collection.mutable.Map[EventMention, Seq[Context]]()
   for (mention <- mentionsSentIds)
   {
     var contextsPerMention = new ArrayBuffer[Context]()
    for (x<-contexts) {
      val sentfreq = ArrayBuffer[Array[Int]]()
      for (y <- x.distanceCount) {
        //take the absolute value of entities in context, and calculate relative distance to this mention
        val rel_distance = (mention.sentence - y(0)).abs
        val freq = y(1)
        sentfreq += Array(rel_distance, freq)
      }
      val ctxt = new Context(x.location, x.entity, sentfreq)
      contextsPerMention += ctxt
    }
     mentionsContexts += (mention -> (contextsPerMention.toSeq))
   }
   mentionsContexts
 }

  //if key exists add+1 to its value, else add 1 as its value
  def checkAddFreq(mapper: scala.collection.mutable.Map[String, Int], key: String,old_freq:Int): scala.collection.mutable.Map[String, Int] = {
    mapper.get(key) match {
      case Some(value) =>
        mapper(key) = value+old_freq
      case None => mapper(key) = old_freq
    }
    mapper
  }
  case class MostFreqEntity(sentId: Int, mention: String, mostFreqEntity: String)

  def findMostFreqContextEntitiesForOneEvent(mention:EventMention, contexts:Seq[Context], entityType:String, n:Int):MostFreqEntity= {
    var entityFreq = scala.collection.mutable.Map[String, Int]()
    var maxFreq = 0
    var mostFreqEntity = ""
    for (x <- contexts) {
      for (y <- x.distanceCount) {
        val sentDist = y(0)
        val freq = y(1)
        if (sentDist <= n && x.entity.contains(entityType)) {
          entityFreq = checkAddFreq(entityFreq, x.location, freq)
          if (entityFreq(x.location) >= maxFreq) {
            maxFreq = entityFreq(x.location)
            mostFreqEntity = x.location
          }
        }
      }
    }
    MostFreqEntity(mention.sentence, mention.words.mkString(" "), mostFreqEntity)
  }
  //for all events, find most the frequent entity (e.g.,Senegal-LOC) within n sentences.
  def findMostFreqContextEntitiesForAllEvents(mentionContextMap: scala.collection.mutable.Map[EventMention, Seq[Context]], n:Int,entityType:String):Seq[MostFreqEntity] = {
    mentionContextMap.keys.toSeq.map(key=>findMostFreqContextEntitiesForOneEvent(key,mentionContextMap(key), entityType,n))
  }

  def printmentionContextMap(mentionContextMap: scala.collection.mutable.Map[String, Seq[Context]]) = {
    for (mnx <- mentionContextMap.keys) {
      println(s"event : $mnx")
      for (x <- mentionContextMap(mnx)) {
        println(s"entity string name : ${x.location}")
        println(s"entity : ${x.entity}")
        print(s"relativeDistance and Count :{")
        for (y <- x.distanceCount) {
          print(s"[${y.mkString(",")}],")
        }
        print(s"}")
        println("\n")
      }
    }
  }

  case class entityNameEntity(entityName: String, entity: String)
  //from the list of all context words extract only ones you are interested in .eg: extract Senegal_B-LOC_0
  def filterSignificantEntities(entitySentFreq: scala.collection.mutable.
  Map[ContextKey, Int]):scala.collection.mutable.Map[entityNameEntity, ArrayBuffer[Array[Int]]]  = {
    val sentIdFreq= scala.collection.mutable.Map[entityNameEntity, ArrayBuffer[Array[Int]]]()
    for (key <- entitySentFreq.keys) {
      val entityName = key.entityName
      val entity = key.entity
      val sentId = key.index
      val freq_old = entitySentFreq(key)
      val nk = entityNameEntity(entityName, entity)
      //if the entity_sentenceid combination already exists in the dictionary, increase its frequency by 1, else add.
      sentIdFreq.get(nk) match {
        case Some(i) =>
          var freq_new = sentIdFreq(nk)
          var sentfreqa = Array(sentId, freq_old)
          freq_new += sentfreqa
          sentIdFreq += (nk -> freq_new)
        case None =>
          val sentfreq = ArrayBuffer[Array[Int]]()
          sentfreq += Array(sentId, freq_old)
          sentIdFreq += (nk -> sentfreq)
      }
    }
    (sentIdFreq)
  }

  //if key exists add+1 to its value, else add 1 as its value
  def checkAddToMap(mapper: scala.collection.mutable.Map[ContextKey, Int], key: ContextKey):Unit= {
    mapper.get(key) match {
      case Some(value) =>
        mapper(key) = value+1
      case None => mapper(key) = 1
    }

  }

  //todo dont use _ split. use instead a case class
  def convertMapToContextSeq(sentIdFreq: scala.collection.mutable.Map[entityNameEntity, ArrayBuffer[Array[Int]]])=
  {
    var contexts = new ArrayBuffer[Context]()
    for (key <- sentIdFreq.keys) {
      val name = key.entityName
      val entity = key.entity
      //relativeSentDistance it will be of the form [int a,int b], where a=relative distance from the nearest mention
      // and b=no of times that context word occurs in that sentence
      val relativeSentDistance=sentIdFreq(key)
      val ctxt = new Context(name, entity,relativeSentDistance)
      contexts += ctxt
    }
    contexts.toSeq
  }
  case class ContextKey(entityName: String, entity: String, index: Int)

  def extractContext(doc: Document): Seq[Context] = {
    //Get all the entities and their named entity types along with the number of times they occur in a sentence
    var entitySentFreq: scala.collection.mutable.Map[ContextKey, Int] = Map()
    for ((s, i) <- doc.sentences.zipWithIndex) {
      var entityCounter = 0
      val indicesToSkip = ArrayBuffer[Int]()
        for ((e, w, n) <- (s.entities.get, s.words, s.norms.get).zipped) {
          var string_entity_sindex:Option[ContextKey] = None
          if (!indicesToSkip.contains(entityCounter)) {
            if (e == "B-LOC" || e=="B-DATE") {
              var entity = e
              var entity_name = w.toLowerCase
              if (e == "B-LOC") {
                entity = "LOC"
                // IF  LOC has multiple tokens. (e.g., United States of America.) merge them to form one entity_name
                var fullName = ArrayBuffer[String]()
                var tempEntity = e
                var tempCounter = entityCounter
                do {
                  fullName += s.words(tempCounter)
                  indicesToSkip += tempCounter
                  tempCounter = tempCounter + 1
                  tempEntity = s.entities.get(tempCounter)
                  entity_name = fullName.mkString(" ").toLowerCase()
                }while (tempEntity == "I-LOC")
                string_entity_sindex=Some(ContextKey(entity_name,entity,i))
              }
              else if (e.contains("B-DATE")) {
                entity = "DATE"
                val split0 = n.split('-').head
                if (split0 != "XXXX" && Try(split0.toInt).isSuccess) {
                  entity_name = split0
                  string_entity_sindex=Some(ContextKey(entity_name,entity,i))
                }
              }
              if (string_entity_sindex.isDefined) {
                checkAddToMap(entitySentFreq, string_entity_sindex.get)
              }
            }
          }
          entityCounter = entityCounter + 1
        }
    }

    val sf=filterSignificantEntities(entitySentFreq)
    val allContexts=convertMapToContextSeq(sf)
    allContexts
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

class Context(var location: String, var entity: String, var distanceCount:ArrayBuffer[Array[Int]])
