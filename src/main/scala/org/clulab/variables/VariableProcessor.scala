package org.clulab.variables

import org.clulab.dynet.Utils
import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.{Document, Processor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer


class Context(var location: String, var entity: String, var distanceCount:ArrayBuffer[Array[Int]])

class VariableProcessor(val processor: Processor, val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))
    val allContexts = extractContext(doc)
    printContexts(allContexts)
    for (x<-mentions) {
      println("*****raw mention value is " + x.raw)
      println("words=" + x.words)
      println("id at which this mention occurs  is" + x.sentence)
    }
    (doc, mentions)
  }

  def printContexts(allContexts: Seq[Context]) = {
    println("length of all contexts is " + allContexts.length)
    for (x <- allContexts) {
      println(s"\n")
      println(s"location : ${x.location}")
      println(s"entity : ${x.entity}")
      print(s"relativeDistance and Count :{")
      for (y<-x.distanceCount)
        {
          print(s"[${y.mkString(",")}],")
        }
      print(s"}")

    }
  }


  //from the format of entitystring_entity_sentenceid->freq, convert to entitystring_entity->([sentenceid1,freq],)
  def extractSentIdFreq(entitySentFreq: Map[String, Int])  = {
    var sentIdFreq= Map[String, ArrayBuffer[Array[Int]]]()
    for (key <- entitySentFreq.keys) {
//      println(s"${key} : ${entitySentFreq(key)}")
      val ks = key.split("_")
//      println(s"ks=${ks} ")
//      println(s"ks.length=${ks.length} ")
      val entityName = ks(0)
//      println(s"ks(0)=${ks(0)} ")
      var entity = ks(1)
      if (entity.containsSlice("LOC")) {
        entity = "LOC"
        val sentId = ks(2).toInt
        val freq_old = entitySentFreq(key).toInt
        val nk = entityName + "_" + entity
        sentIdFreq.get(nk) match {
          case Some(i) => {
            var freq_new= sentIdFreq(nk)
            var sentfreqa = Array(sentId,freq_old)
            freq_new += sentfreqa
            sentIdFreq+=(key-> freq_new)
          }
          case None => {
            val sentfreq = ArrayBuffer[Array[Int]]()
            sentfreq += Array(sentId, freq_old)
            sentIdFreq += (nk -> sentfreq)
          }
        }
      }
    }
    (sentIdFreq)
  }

  //if key exists add+1 to its value, else add 1 as its value
  def checkAddToMap(mapper: Map[String, Int], key: String): Map[String, Int] = {
    mapper.get(key) match {
      case Some(i) => {
        var freq = mapper(key)
        freq = freq + 1
        mapper(key) = freq
      }
      case None => mapper(key) = 1
    }
    (mapper)
  }

  def convertMapToContextSeq(sentIdFreq: Map[String, ArrayBuffer[Array[Int]]])=
  {
    var contexts = new ArrayBuffer[Context]()
    for (key <- sentIdFreq.keys) {
      val ks = key.split("_")
      val name = ks(0)
      val entity = ks(1)
      val abSentFreq=sentIdFreq(key)
      val ctxt = new Context(name, entity,abSentFreq)
      contexts += ctxt
    }
    (contexts.toSeq)
  }

  def extractContext(doc: Document): Seq[Context] = {

    var counter: Map[String, Int] = Map()
    var entitySentFreq: Map[String, Int] = Map()
    for ((s, i) <- doc.sentences.zipWithIndex) {
      //val bLocIndexes = s.entities.get.indices.filter(index => s.entities.get(index) == "B-LOC")
      for ((es, ix) <- s.entities.get.zipWithIndex) {
        val string_entity_sindex = s.words(ix).toLowerCase + "_" + es + "_" + i.toString
        counter = checkAddToMap(entitySentFreq, string_entity_sindex)
      }
    }
    val sf=extractSentIdFreq(entitySentFreq)
    val allContexts=convertMapToContextSeq(sf)
    (allContexts)
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
