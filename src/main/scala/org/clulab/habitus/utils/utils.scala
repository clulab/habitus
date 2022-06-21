package org.clulab.habitus

import org.clulab.odin.{EventMention, Mention, RelationMention, TextBoundMention}
import org.clulab.processors.{Document, Sentence}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

package object utils {

  def displayMentions(mentions: Seq[Mention], doc: Document): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    println
    for ((s, i) <- doc.sentences.zipWithIndex) {
      println(s"sentence #$i")
      println(s.getSentenceText)
      println("Tokens: " + (s.words.indices, s.words, s.tags.get).zipped.mkString(", "))
      println("Lemmas: " + (s.lemmas.get.indices, s.lemmas.get).zipped.mkString(", "))
      println("Entities: " + s.entities.get.mkString(", "))
      println("Norms: " + s.norms.get.mkString(", "))
      println("Chunks: " + s.chunks.get.mkString(", "))
      printHybridDependencies(s)
      println

      val sortedMentions = mentionsBySentence(i).sortBy(_.label)
      val (events, entities) = sortedMentions.partition(_ matches "Event")
      val (tbs, rels) = entities.partition(_.isInstanceOf[TextBoundMention])
      val sortedEntities = tbs ++ rels.sortBy(_.label)
      println("entities:")
      sortedEntities foreach displayMention

      println
      println("events:")
      events foreach displayMention
      println("=" * 50)
    }
  }

  //todo:context details class should store all histograms
  case class ContextDetails(mention: String, mostFreqLoc0Sent: String, mostFreqLoc1Sent: String, mostFreqLoc: String,
                            mostFreqDate0Sent: String, mostFreqDate1Sent: String, mostFreqDate: String,
                            mostFreqCrop0Sent: String,
                            mostFreqCrop1Sent: String,
                            mostFreqCrop: String,
                            mostFreqFertilizer0Sent: String,
                            mostFreqFertilizer1Sent: String,
                            mostFreqFertilizerOverall: String)

  //some sentences might have multiple event mentions in it, and
  def addAllContextForGivenSentId(context: mutable.Map[Int, ArrayBuffer[ContextDetails]],
                                  sentId: Int, allContexts: ArrayBuffer[String]): Unit = {

    for (ctxt <- context(sentId)) {
      allContexts.append(ctxt.mostFreqLoc0Sent)
    }
  }

  def printHybridDependencies(s:Sentence): Unit = {
    if (s.hybridDependencies.isDefined) {
      println(s.hybridDependencies.get.toString)
    }
  }

  def printSyntacticDependencies(s: Sentence): Unit = {
    if (s.dependencies.isDefined) {
      println(s.dependencies.get.toString)
    }
  }


  def displayMention(mention: Mention): Unit = {
    val boundary = s"\t${"-" * 30}"
    println(s"${mention.labels} => ${mention.text}")
    println(boundary)
    println(s"\tRule => ${mention.foundBy}")
    val mentionType = mention.getClass.toString.split("""\.""").last
    println(s"\tType => $mentionType")
    println(boundary)
    mention match {
      case tb: TextBoundMention =>
        println(s"\t${tb.labels.mkString(", ")} => ${tb.text}")
        tb.norms.head.foreach { x =>
          println(s"\tNorm => $x")
        }
      case em: EventMention =>
        println(s"\ttrigger => ${em.trigger.text}")
        displayArguments(em)
      case rel: RelationMention =>
        displayArguments(rel)
      case _ => ()
    }

    if (mention.attachments.nonEmpty) {
      println(s"$boundary\nAttachments:")
      for (att <- mention.attachments) {
        println(att)
      }
    }
    println(s"$boundary\n")
  }


  def displayArguments(b: Mention): Unit = {
    b.arguments foreach {
      case (argName, ms) =>
        ms foreach { v =>
          println(s"\t$argName ${v.labels.mkString("(", ", ", ")")} => ${v.text}")
        }
    }
  }
}
