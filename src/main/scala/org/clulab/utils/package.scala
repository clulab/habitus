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

  // extract needed information and write them to tsv in a desired format. Return nothing here!
  def outputMentionsToTSV(mentions: Seq[Mention], doc: Document, filename: String, pw: PrintWriter): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {
      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val sortedMentions = mentionsBySentence(i).filter(_.label matches "Assignment")
      sortedMentions.foreach{
          // Format to print: variable \t value text \t value norms \t extracting sentence \t document name \n
          // Since we only focus on the Assignment mention which includes two submentions in the same format called
          // ``variable`` and ``value`` we access the two through ``arguments`` attribute of the Mention class.
          m => try {
            val varText = m.arguments("variable").head.text
            val value = m.arguments("value").head
            val valText = value.text
            val sentText = s.getSentenceText
            val valNorms = value.norms

            val norm =
              if(valNorms.isDefined && valNorms.get.size > 2) {
                valNorms.filter(_.length > 2).get(0)
              } else {
                //
                // not all NEs have meaningful norms set
                //   For example, DATEs have norms, but CROPs do not
                // in the latter case, we revert to the lemmas or to the actual text as a backoff
                //
                if(value.lemmas.isDefined) {
                  value.lemmas.get.mkString(" ")
                } else {
                  value.text
                }
              }

            //println(s"NORMS for ${valText}: [$norm]")

            pw.println(s"$varText\t$valText\t$norm\t$sentText\t$filename")
          } catch {
            case e: NoSuchElementException =>
              println(s"No normalized value found for ${m.arguments("value").head.text} in sentence ${s.getSentenceText}!")
              e.printStackTrace()
            case e: RuntimeException =>
              println(s"Error occurs for sentence: ${s.getSentenceText}")
              e.printStackTrace()
          }
          println(m.arguments("value").head.norms.filter(_.length > 2))
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
