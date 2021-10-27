package org.clulab

import org.clulab.odin._
import org.clulab.processors.{Sentence, Document}
import java.io._

package object utils {

  def displayMentions(mentions: Seq[Mention], doc: Document): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    println
    for ((s, i) <- doc.sentences.zipWithIndex) {
      println(s"sentence #$i")
      println(s.getSentenceText)
      println("Tokens: " + (s.words.indices, s.words, s.tags.get).zipped.mkString(", "))
      println("Entities: " + s.entities.get.mkString(", "))
      println("Norms: " + s.norms.get.mkString(", "))
      printSyntacticDependencies(s)
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
  case class contextDetails( mention: String,mostFreqLoc0Sent: String,mostFreqLoc1Sent: String, mostFreqLoc: String,
                             mostFreqDate0Sent: String,mostFreqDate1Sent: String, mostFreqDate: String,
                             mostFreqCrop0Sent:String,
                             mostFreqCrop1Sent:String,
                             mostFreqCrop:String)


  // extract needed information and write them to tsv in a desired format. Return nothing here!
  def outputMentionsToTSV(mentions: Seq[Mention], doc: Document,context:scala.collection.mutable.Map[Int,contextDetails],
                          filename: String, pw: PrintWriter): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val sortedMentions = mentionsBySentence(i).filter(_.label matches "Assignment")


      sortedMentions.foreach{
          // Format to print: variable \t value text \t value norms \t extracting sentence \t document name
        // \t Most frequent LOC within 0 sentences \t Most frequent LOC within 1 sentences.\t Most frequent LOC anywhere in the doc.\n
          // Since we only focus on the Assignment mention which includes two submentions in the same format called
          // ``variable`` and ``value`` we access the two through ``arguments`` attribute of the Mention class.
          m => try {
            pw.println(s"${m.arguments("variable").head.text}\t${m.arguments("value").head.text}\t${m.arguments("value")
              .head.norms.filter(_.length > 2).get(0)}\t${s.getSentenceText}\t$filename\t${
              context(i).mostFreqLoc0Sent}\t${
              context(i).mostFreqLoc1Sent}\t${
              context(i).mostFreqLoc}\t${
              context(i).mostFreqDate0Sent}\t${
              context(i).mostFreqDate1Sent}\t${
              context(i).mostFreqDate}\t${
              context(i).mostFreqCrop0Sent}\t${
              context(i).mostFreqCrop1Sent}\t${
              context(i).mostFreqCrop}")
          } catch {
            case e: NoSuchElementException => println(s"No normalized value found for ${m.arguments("value").head.text} in sentence ${s.getSentenceText}!")
              e.printStackTrace(System.out)
            case e: RuntimeException => println(s"Error occurs for sentence: ${s.getSentenceText}")
              e.printStackTrace(System.out)
          }
            println(m.arguments("variable").head.text)
            println(m.arguments("value").head.text)
            println(m.arguments("value").head.norms.filter(_.length > 2))
            println(m.arguments("value").head.norms)
      }
    }
  }



  def printSyntacticDependencies(s:Sentence): Unit = {
    if(s.dependencies.isDefined) {
      println(s.dependencies.get.toString)
    }
  }


  def displayMention(mention: Mention) {
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
        tb.norms.head.foreach {x =>
          println(s"\tNorm => $x")
        }
      case em: EventMention =>
        println(s"\ttrigger => ${em.trigger.text}")
        displayArguments(em)
      case rel: RelationMention =>
        displayArguments(rel)
      case _ => ()
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
