package org.clulab.habitus.variables

import org.clulab.dynet.Utils
import org.clulab.odin.EventMention
import org.clulab.odin.ExtractorEngine
import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.processors.Processor
import org.clulab.processors.Sentence
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.utils.FileUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import scala.util.control.Breaks._


class EntityHistogramExtractor(val processor: Processor, val extractor: ExtractorEngine) {

  def extractHistogramEventMentions(doc: Document, mentions:Seq[Mention]):(Seq[EventMention],Seq[EntityDistFreq])= {
    //collect all event mentions only (and not text bound ones)
    val allEventMentions = mentions.collect { case m: EventMention => m }

    //get histogram of all Entities (refer  case class Entity)
    //histogram e.g.,{Senegal, LOC, {[1, 1], [4, 2]}}-
    // which means, the Location Sengal occurs in sentence 1 once,in sentence 4, 2 times setc
    (allEventMentions,getEntityFreqPerSent(doc))
  }









  // a case class to hold the entity value and its ner tag together
  case class EntityNameNerTag(entityValue: String, nerTag: String)

  //for each entity, find which sentence they occur in an how many times
  def mapEntityToFreq(entitySentFreq: mutable.Map[Entity, Int]):Seq[EntityDistFreq]  = {
    val sentIdFreq= mutable.Map[EntityNameNerTag, ArrayBuffer[(Int,Int)]]()
    for (key <- entitySentFreq.keys) {
      val entityName = key.entityValue
      val entity = key.tag
      val sentId = key.sentIdx
      val freq = entitySentFreq(key)
      val newkey = EntityNameNerTag(entityName, entity)
      //if the entity_sentenceid combination already exists in the dictionary, increase its frequency by 1, else add.
      sentIdFreq.get(newkey) match {
        case Some(_) =>
          val freqNew = sentIdFreq(newkey)
          val sentfreqa = (sentId, freq)
          freqNew.append(sentfreqa)
          sentIdFreq += (newkey -> freqNew)
        case None =>
          val sentfreq = ArrayBuffer[(Int,Int)]()
          sentfreq.append((sentId, freq))
          sentIdFreq += (newkey -> sentfreq)
      }
    }
    convertMapToSeq(sentIdFreq)
  }

  //if key exists add+1 to its value, else add 1 as its value
  def checkIncreaseFreq(map: mutable.Map[Entity, Int], key: Entity): Unit =
      map(key) = map.getOrElse(key, 0) + 1

  def convertMapToSeq(sentIdFreq: mutable.Map[EntityNameNerTag, ArrayBuffer[(Int,Int)]]): Seq[EntityDistFreq] = sentIdFreq
      .map { case (key, value) =>
        EntityDistFreq(key.entityValue, key.nerTag, value)
      }
      .toSeq

  //details of each entity: name,ner tag, index of sentence it was found in
  case class Entity(entityValue: String, tag: String, sentIdx: Int)

  // IF an LOC entity has multiple tokens. (e.g., United States of America.) merge them to form one entityName
  def checkForMultipleTokens(nerTag:String,entityCounter:Int,indicesToSkip:ArrayBuffer[Int],entityName:String,sent:Sentence,insideTag:String): String = {
    var newEntityName = entityName
    val fullName = ArrayBuffer[String]()
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
      } while (tempEntity == insideTag)
    }
    newEntityName
  }

  //For each entity find which sentence it occurs in and its frequency
  def getEntityFreqPerSent(doc: Document): Seq[EntityDistFreq] = {
    //for each sentence how many times does this entity occur
    val entitySentFreq = mutable.Map.empty[Entity, Int]
    for ((s, i) <- doc.sentences.zipWithIndex) {
      var entityCounter = 0
      //some indices have to be skipped if the entity has multiple tokens e.g.,"United States of America"
      val indicesToSkip = ArrayBuffer[Int]()
      for ((nerTag, word, norm) <- (s.entities.get, s.words, s.norms.get).zipped) {
        var EntityNameIndex: Option[Entity] = None
        if (!indicesToSkip.contains(entityCounter)) {
          var namedEntityTag = ""
          var entityName = word.toLowerCase
          //todo: this needs to be passed from user
          //todo: change this to a switch statement?
          if (nerTag == "B-LOC") {
            val cleanNerTag = "LOC"
            val newEntityName = checkForMultipleTokens(nerTag, entityCounter, indicesToSkip, entityName, s,"I-LOC")
            EntityNameIndex = Some(Entity(newEntityName, cleanNerTag, i))
          }
          if (nerTag == "B-CROP") {
            val cleanNerTag = "CROP"
            val newEntityName = checkForMultipleTokens(nerTag, entityCounter, indicesToSkip, entityName, s,"I-CROP")
            EntityNameIndex = Some(Entity(newEntityName, cleanNerTag, i))
          }
          if (nerTag == "B-FERTILIZER") {
            val cleanNerTag = "FERTILIZER"
            val newEntityName = checkForMultipleTokens(nerTag, entityCounter, indicesToSkip, entityName, s,"I-FERTILIZER")
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
        //a counter to keep track of number of entities seen in this sentence- useful in skipindex for multiple tokens
        entityCounter = entityCounter + 1
      }
    }
    //calculate frequency across all sentences, return as entityAbsDistFreq (e.g.,{Senegal, LOC, {[1, 1], [4, 2]}})
    mapEntityToFreq(entitySentFreq)
  }
}

object EntityHistogramExtractor {
  def apply(): EntityHistogramExtractor = {
    // Custom NER for variable reading
    val kbs = Seq(
      "lexicons/FERTILIZER.tsv"
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
    val rules = FileUtils.getTextFromResource("/variables/master.yml")
    // creates an extractor engine using the rules and the default actions
    val extractor = ExtractorEngine(rules)

    new EntityHistogramExtractor(processor, extractor)
  }
}
// histogram of all Entities
//e.g.,{Senegal, LOC, {[1, 1], [4, 2]}}- The Location Sengal occurs in sentence 1 once,in sentence 4, 2 times setc
case class EntityDistFreq(entityValue: String, nerTag: String, entityDistFrequencies:ArrayBuffer[(Int,Int)])
