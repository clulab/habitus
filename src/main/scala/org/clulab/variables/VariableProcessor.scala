package org.clulab.variables

import org.clulab.dynet.Utils
import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.{Document, Processor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import scala.collection.mutable.Map
import scala.collection.mutable.ArrayBuffer


class Context(var location: String, var entity: String, var relativeDist: Int, var count: Int)

class VariableProcessor(val processor: Processor, val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))
    val allContexts = extractContext(doc)
    printContexts(allContexts)
    (doc, mentions)
  }
  def printContexts(allContexts: Seq[Context] ) = {
    for (x <- allContexts) {
      println(s"location : ${x.location}")
      println(s"entity : ${x.entity}")
      println(s"relativeDist : ${x.relativeDist}")
      println(s"count : ${x.count}")

    }
  }
  def checkAddCounter(counter: Map[String, Int], country:String ): Map[String, Int] = {
    counter.get(country) match {
      case Some(i) => {
        var freq = counter(country)
        freq = freq + 1
        counter(country) = freq
      }
      case None =>  counter(country) = 1
    }
    (counter)
  }



  def extractContext(doc: Document): Seq[Context] = {
    var contexts = new ArrayBuffer[Context]()
    var counter: Map[String, Int] = Map()
    for ((s, i) <- doc.sentences.zipWithIndex) {
      //println(s"sentence #$i")
      //println(s.getSentenceText)
      //println("Entities: " + s.entities.get.mkString(", "))
      val bLocIndexes = s.entities.get.indices.filter(index => s.entities.get(index) == "B-LOC")
      println(s"value of blocindexes is $bLocIndexes")
      //val lengths = bLocIndexes.map { start => countLocs(start) }
      //println("Tokens: " + (s.words(3)))
      for ((es, ix) <- s.entities.get.zipWithIndex) {
        if (es == "B-LOC") {
          //println("Tokens: " + (s.words(ix)))
          counter=checkAddCounter(counter,s.words(ix))
          //println("value in counter: " + (counter(s.words(ix))))
          val ctxt = new Context(s.words(ix), "LOC", ix, counter(s.words(ix)))
          contexts += ctxt
        }
      }
    }
    (contexts.toSeq)
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
