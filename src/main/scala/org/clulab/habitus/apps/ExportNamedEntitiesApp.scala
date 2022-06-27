package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.variables.VariableProcessor.resourceDir
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.struct.Counter
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import java.io.File
import scala.util.{Failure, Try}

object ExportNamedEntitiesApp extends App {
  val inputFileName = args.lift(0).getOrElse("../docs/animate_sent3.tsv")
  val outputFileName = args.lift(1).getOrElse("../docs/animate_sent3.conll")

  val useHabitus = true

  def newLexiconNer(): LexiconNER = {
    val kbs = Seq(
      "lexicons/FERTILIZER.tsv", // VariableProcessor
      "lexicons/CROP.tsv",       // VariableProcessor
      "lexicons/ACTOR.tsv"       // BeliefProcessor & InterviewsProcessor
    )
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true, // FERTILIZER is case insensitive.
        true, // CROP
        true  // ACTOR
      ),
      None
    )

    lexiconNer
  }

  val processor = {
    Utils.initializeDyNet()

    if (useHabitus) new HabitusProcessor(Some(newLexiconNer()), filter = false)
    else new CluProcessor()
  }
  val badNamedEntities = Array(
    "ssc",
    "tel", // This text does not exist!
    "agriculture and rural equipment",
    "however",
    "weekly monitoring",
    "north zone",
    "( source : weekly tracking , matam delegation ) . development", // This text does not exist!
    "cold dry",
    "left bank of the senegal",
    "fax", // This text does not exist!
    "kollangal", // This text does not exist!
    "bakel delegationii . 2 . development", // This text does not exist.
    "( year2 lm12 ) : forecasts",
    "source : weekly",
    "bakel delegation iii . 2 . development", // This text does not exist.
    "wintering campaign national company",
    "pete",
    "left bank of the senegal river",
    "the cold dry",
    "drdr",
    "pos",
    "imv",
    "dry season",
    // "dpv <- correct but unimportant",
    "i", // This text does not exist!
    "( yr3 lm12 ) : development",
    "i . 2 development", // This text does not exist.
    "financing of the agricultural production ( fpa )"
  )
  val badNamedEntityWords = badNamedEntities.map(_.split(' '))
  val counter = new Counter[Seq[String]]()

  Sourcer.sourceFromFile(new File(inputFileName)).autoClose { source =>
    val tsvReader = new TsvReader()

    FileUtils.printWriterFromFile(new File(outputFileName)).autoClose { printWriter =>
      printWriter.print("-DOCSTART-\t0\n\t\n")
      source.getLines.drop(1).foreach { line =>
        println(line)
        val Array(_ /*animate*/, _ /*correct*/, _ /*ranking*/, _ /*comments*/, text) = tsvReader.readln(line)
        val document = processor.mkDocument(text)
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
          val words = document.sentences.head.words
          val foundBadNamedEntityWords = badNamedEntityWords.filter(words.containsSlice(_))

          foundBadNamedEntityWords.foreach(counter.incrementCount(_))
          if (foundBadNamedEntityWords.nonEmpty) {
            processor.annotate(document)
            val oldEntities = document.sentences.head.entities.get
            val newEntities = {
              val newEntities = oldEntities.clone()

              foundBadNamedEntityWords.foreach { badNamedEntityWords =>
                val index = words.indexOfSlice(badNamedEntityWords)

                index.until(index + badNamedEntityWords.length).foreach { index =>
                  if (newEntities(index) != "O")
                    newEntities(index) = "O"
                }
              }
              newEntities
            }
            // == on Arrays doesn't seem to work.
            val changed = words.indices.exists { index => newEntities(index) != oldEntities(index) }
            if (changed) {
              words.indices.foreach { index =>
                printWriter.print(s"${words(index)}\t${newEntities(index)}\t${oldEntities(index)}\n")
              }
              printWriter.print("\t\n")
            }
          }
        }
      }
    }
  }
  badNamedEntityWords.foreach { badNamedEntity =>
    val key = badNamedEntity
    val value = counter.getCount(key)

    println(s"${key.mkString(" ")}\t$value")
  }
}
