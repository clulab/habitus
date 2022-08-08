package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.struct.Counter
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, Sourcer, StringUtils}
import org.clulab.wm.eidoscommon.utils.TsvReader

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Try}

object ExportNamedEntities2App extends App {
  val inputFileName = args.lift(0).getOrElse("../corpora/SAED100/error_analysis/baseline_non_entities.csv")
  val inputDirName = args.lift(1).getOrElse("../corpora/SAED100")
  val outputFileName = args.lift(1).getOrElse("../docs/SAED100.conll")

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

    Utils.initializeDyNet()
    new HabitusProcessor(Some(lexiconNER), filter = false)
  }
  val badNamedEntities: Array[String] = loadNamedEntities(inputFileName)
  val badNamedEntityWords: Array[Array[String]] = parseNamedEntities(badNamedEntities)
  val badNamedEntityFoundCounts = new Counter[Seq[String]]()
  val badNamedEntityCorrectedCounts = new Counter[Seq[String]]()

  def loadNamedEntities(inputFileName: String): Array[String] = {

    def isOdd(string: String): Boolean = string.count(_ == '"') % 2 != 0

    val fields = Sourcer.sourceFromFile(new File(inputFileName)).autoClose { source =>
      val lines = source.getLines().drop(1).toVector // skip header
      val recordIndexes = lines.zipWithIndex.scanLeft((-1, false, 0)) { case ((recordIndex: Int, inside: Boolean, _lineIndex: Int), (line: String, lineIndex: Int)) =>
        val odd = isOdd(line)

        if (inside) (recordIndex, !odd, lineIndex) // same record and stay inside if not add
        else (recordIndex + 1, odd, lineIndex) // next record and inside if odd
      }
      val recordIndexGroups = recordIndexes.drop(1).groupBy(_._1)
      val records = Range(0, recordIndexGroups.size).map { recordIndex =>
        val record = recordIndexGroups(recordIndex).map { case (_, _, lineIndex) =>
          lines(lineIndex)
        }

        record.mkString("\n")
      }
      val fields = records.map(StringUtils.afterFirst(_, ',', false))

      fields
    }
    fields.toArray
  }

  def parseNamedEntities(fields: Array[String]): Array[Array[String]] = {
    val splitFields: Array[String] = fields.flatMap { field =>
      if (field.startsWith("\"")) {
        require(field.endsWith("\""))
        field
            .drop(1)
            .dropRight(1)
            .replaceAll("\"\"", "\"")
            .split(",")
            .map(_.trim)
      }
      else
        Array(field)
    }

    splitFields
        .filterNot(_.endsWith(" (Correct Entity)"))
        .map(_.split(' '))
  }

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
