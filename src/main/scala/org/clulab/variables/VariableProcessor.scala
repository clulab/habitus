package org.clulab.variables

import org.clulab.dynet.Utils
import org.clulab.odin.{EventMention,ExtractorEngine,Mention,TextBoundMention}
import org.clulab.processors.{Document, Processor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import util.control.Breaks._
import scala.collection.mutable.{Map,ArrayBuffer}
import scala.util.Try

class Context(var location: String, var entity: String, var distanceCount:ArrayBuffer[Array[Int]])

class VariableProcessor(val processor: Processor, val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))
    val mse=extractContextAndFindMostFrequentEntity(doc,mentions,Int.MaxValue,"DATE")
    (doc, mentions)
  }

  def extractContextAndFindMostFrequentEntity(doc: Document,mentions:Seq[Mention],n:Int,entity_type:String )={
    //map each event mention to its sentence id. useful in extracting contexts
    val mentionsSentIds=  scala.collection.mutable.Map[String, Int]()
    //todo: ask keith how to do collect instead of breakable.
    // val sentIds = mentions.collect { case m: EventMention => m.sentence }


    for (x<-mentions) {
      breakable {
        x match {
          case m: TextBoundMention => break
          case m: EventMention => mentionsSentIds += (x.words.mkString(" ")->x.sentence)
        }
      }
    }

    // allContexts= map of all context entities (e.g.,Senegal) to the ids of sentences that they occur at
    val allContexts = extractContext(doc)
    val mentionContextMap=mapMentionsToContexts(mentionsSentIds,allContexts)


    //pick entityType from ["LOC","DATE"]; set n=Int.MaxValue to get overall context/frequency in whole document
    findMostFreqContextEntitiesForAllEvents(mentionContextMap,n, entity_type)
  }
  //map each mention to and how far an entity occurs, and no of times it occurs in that sentence
 def mapMentionsToContexts(mentionsSentIds: scala.collection.mutable.Map[String, Int],contexts:Seq[Context])= {
   val mentionsContexts=  scala.collection.mutable.Map[String, Seq[Context]]()
   for (mention <- mentionsSentIds.keys)
   {
     var contextsPerMention = new ArrayBuffer[Context]()
    for (x<-contexts) {
      val sentfreq = ArrayBuffer[Array[Int]]()
      for (y <- x.distanceCount) {
        //take the absolute value of entities in context, and calculate relative distance to this mention
        val rel_distance = (mentionsSentIds(mention) - y(0)).abs
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
      case Some(i) =>
        var freq = mapper(key)
        freq = freq + old_freq
        mapper(key) = freq
      case None => mapper(key) = old_freq
    }
    mapper
  }

  def findMostFreqContextEntitiesForOneEvent(mention:String, contexts:Seq[Context], entityType:String, n:Int)=
  {
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
    mostFreqEntity
  }
  //for all events, find most the frequent entity (e.g.,Senegal-LOC) within n sentences.
  def findMostFreqContextEntitiesForAllEvents(mentionContextMap: scala.collection.mutable.Map[String, Seq[Context]], n:Int,entityType:String):Seq[String] = {
    mentionContextMap.keys.toSeq.map(key=>findMostFreqContextEntitiesForOneEvent(key,mentionContextMap(key), entityType,n))
  }
//todo: ask keith what this is aboutThe first map is keyed by entity name, entity, and sentence index.  In filterSignificantEntities these are aggregated into a key of just entity name and entity.  I don't see any filtering of "significant" things going on there.  Is something missing?  It makes me wonder if two maps are really necessary.
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


  //from the list of all context words extract only ones you are interested in .eg: extract Senegal_B-LOC_0
  def filterSignificantEntities(entitySentFreq: scala.collection.mutable.Map[String, Int])  = {
    val sentIdFreq= scala.collection.mutable.Map[String, ArrayBuffer[Array[Int]]]()
    for (key <- entitySentFreq.keys) {
      val ks = key.split("_")
      val entityName = ks(0)
      val entity = ks(1)
      val sentId = ks(2).toInt
      val freq_old = entitySentFreq(key)
      val nk = entityName + "_" + entity
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
  def checkAddToMap(mapper: scala.collection.mutable.Map[String, Int], key: String): scala.collection.mutable.Map[String, Int] = {
    mapper.get(key) match {
      case Some(i) =>
        var freq = mapper(key)
        freq = freq + 1
        mapper(key) = freq
      case None => mapper(key) = 1
    }
    mapper
  }

  def convertMapToContextSeq(sentIdFreq: scala.collection.mutable.Map[String, ArrayBuffer[Array[Int]]])=
  {
    var contexts = new ArrayBuffer[Context]()
    for (key <- sentIdFreq.keys) {
      val ks = key.split("_")
      val name = ks(0)
      val entity = ks(1)
      //relativeSentDistance it will be of the form [int a,int b], where a=relative distance from the nearest mention
      // and b=no of times that context word occurs in that sentence
      val relativeSentDistance=sentIdFreq(key)
      val ctxt = new Context(name, entity,relativeSentDistance)
      contexts += ctxt
    }
    (contexts.toSeq)
  }

  def extractContext(doc: Document): Seq[Context] = {
    //Get all the entities and their named entity types along with the number of times they occur in a sentence
    var entitySentFreq: scala.collection.mutable.Map[String, Int] = Map()
    for ((s, i) <- doc.sentences.zipWithIndex) {
      var entityCounter = 0
      val indicesToSkip = ArrayBuffer[Int]()
        for ((es, ws, ns) <- (s.entities.get, s.words, s.norms.get).zipped) {
          var string_entity_sindex = ""
          if (!indicesToSkip.contains(entityCounter)) {
            if (es == "B-LOC" || es=="B-DATE") {
              var entity = es
              var entity_name = ws.toLowerCase
              if (es == "B-LOC") {
                entity = "LOC"
                // IF  LOC has multiple tokens. (e.g., United States of America.) merge them to form one entity_name
                var fullName = ArrayBuffer[String]()
                var temp_entity = es
                var temp = entityCounter
                //todo: replace negative check of o with if I-Loc- what if you see another B-LOC
                while (temp_entity != "O") {
                  fullName += s.words(temp)
                  indicesToSkip += temp
                  temp = temp + 1
                  temp_entity = s.entities.get(temp)
                  entity_name = fullName.mkString(" ").toLowerCase()
                }
                string_entity_sindex = entity_name + "_" + entity + "_" + i.toString
              }
              else if (es.contains("B-DATE")) {
                entity = "DATE"
                val split0 = ns.split("-")(0)
                if (split0 != "XXXX" && Try(split0.toInt).isSuccess) {
                  entity_name = split0
                  string_entity_sindex = entity_name + "_" + entity + "_" + i.toString
                }
              }
              if (string_entity_sindex != "") {
                entitySentFreq = checkAddToMap(entitySentFreq, string_entity_sindex)
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

object VariableProcessor {
  def apply(): VariableProcessor = {
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

    new VariableProcessor(processor, extractor)
  }
}
