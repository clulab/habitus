package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.struct.Counter
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import java.io.File
import scala.util.{Failure, Try}

object ExportNamedEntitiesApp extends App {
//  val inputFileName = args.lift(0).getOrElse("../docs/animate_sent_new.tsv")
//  val outputFileName = args.lift(1).getOrElse("../docs/animate_sent_new.conll")

//  val inputFileName = args.lift(0).getOrElse("../docs/animate_sent3.tsv")
//  val outputFileName = args.lift(1).getOrElse("../docs/animate_sent3.conll")

  // This one is a combination of the two above.
  val inputFileName = args.lift(0).getOrElse("../docs/animate_sent_new_3.tsv")
  val outputFileName = args.lift(1).getOrElse("../docs/animate_sent_new_3.conll")

  val processor = {
    val lexiconNER = LexiconNER(
      Seq(
        "lexicons/FERTILIZER.tsv", // VariableProcessor
        "lexicons/CROP.tsv",       // VariableProcessor
        "lexicons/ACTOR.tsv"       // BeliefProcessor & InterviewsProcessor
      ),
      Seq(
        true, // FERTILIZER is case insensitive.
        true, // CROP
        true  // ACTOR
      ),
      None
    )

    new HabitusProcessor(Some(lexiconNER), filter = false)
  }
  val badNamedEntities = Array(
    "ssc",
    "tel", // This text does not exist!
    "agriculture and rural equipment",
    "however",
    "weekly monitoring",
    "north zone",
    "( source : weekly tracking , matam delegation ) .", // development", // This text does not exist!
    "cold dry",
    "left bank of the senegal",
    "fax", // This text does not exist!
    "kollangal",
    "bakel delegationii . 2 . development", // This text does not exist.
    "( year2 lm12 ) : forecasts",
    "source : weekly",
    "bakel delegationiii . 2 . development", // This text does not exist.
    "wintering campaign national company",
    "pete",
    "left bank of the senegal river",
    "the cold dry",
    "drdr",
    "pos",
    "imv",
    "dry season",
    "dpv",
    "i",
    "( yr3 lm12 ) : development",
    "i.2 development",
    "financing of the agricultural production ( fpa )"
  )
  val badNamedEntityWords = badNamedEntities.map(_.split(' '))
  val badNamedEntityFoundCounts = new Counter[Seq[String]]()
  val badNamedEntityCorrectedCounts = new Counter[Seq[String]]()

  Sourcer.sourceFromFile(new File(inputFileName)).autoClose { source =>
    val tsvReader = new TsvReader()

    FileUtils.printWriterFromFile(new File(outputFileName)).autoClose { printWriter =>
      printWriter.print("-DOCSTART-\t0\n\t\n")
      source.getLines.drop(1).foreach { line =>
        println(line)
        val Array(_ /*animate*/, _ /*correct*/, _ /*ranking*/, _ /*comments*/, text) = tsvReader.readln(line)
        val document = processor.mkDocumentWithRestoreCase(text) // .mkDocument(text)
        val trial =
            if (document.sentences.length > 0) {
              val trial = Try { processor.annotate(document) }

              if (trial.isFailure)
                println("Failed!")
              trial
            }
            else
              Failure(new RuntimeException("There should be sentences."))

        if (trial.isSuccess && document.sentences.length == 1) {
          val words = document.sentences.head.words.map(_.toLowerCase) // because of restoreCase
          val foundBadNamedEntityWords = badNamedEntityWords.filter(words.containsSlice(_))

          if (foundBadNamedEntityWords.nonEmpty) {
            foundBadNamedEntityWords.foreach(badNamedEntityFoundCounts.incrementCount(_))
            processor.annotate(document)
            val oldEntities = document.sentences.head.entities.get
            val newEntities = {
              val newEntities = oldEntities.clone()

              foundBadNamedEntityWords.foreach { badNamedEntityWords =>
                val index = words.indexOfSlice(badNamedEntityWords)
                val range = Range(index, index + badNamedEntityWords.length)
                val corrected = range.exists { index => oldEntities(index) != "O" }

                if (corrected) {
                  range.foreach { index => newEntities(index) = "O" }
                  badNamedEntityCorrectedCounts.incrementCount(badNamedEntityWords)
                }
              }
              newEntities
            }
            // == on Arrays doesn't work.
            val changed = words.indices.exists { index => newEntities(index) != oldEntities(index) }

            if (changed) {
              words.indices.foreach { index =>
//                printWriter.print(s"${words(index)}\t${oldEntities(index)}\t${newEntities(index)}\n")
                printWriter.print(s"${words(index)}\t${newEntities(index)}\n")
              }
              printWriter.print("\t\n")
            }
          }
        }
      }
    }
  }
  println("key\tfound\tcorrected")
  badNamedEntityWords.foreach { badNamedEntity =>
    val key = badNamedEntity
    val found = badNamedEntityFoundCounts.getCount(key)
    val corrected = badNamedEntityCorrectedCounts.getCount(key)

    println(s"${key.mkString(" ")}\t$found\t$corrected")
  }
}
