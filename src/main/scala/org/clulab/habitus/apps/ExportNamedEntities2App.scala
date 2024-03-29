package org.clulab.habitus.apps

import org.clulab.struct.Counter
import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import java.io.{File, PrintWriter}
import scala.io.BufferedSource
import scala.util.Using

object ExportNamedEntities2App extends App {
  val inputFileName = args.lift(0).getOrElse("../corpora/SAED100/error_analysis/baseline_non_entities.csv")
  val inputDirName = args.lift(1).getOrElse("../corpora/SAED100/SAED100-linux")
  val correctedOutputFileName = args.lift(1).getOrElse("../corpora/SAED100/SAED100.conll")
  val uncorrectedOutputFileName = correctedOutputFileName + ".uncorrected"

  val badNamedEntityWords: Array[Array[String]] = new NonEntitiesFile(inputFileName).load()
  badNamedEntityWords.foreach { words => println(words.mkString(" ")) }
  val badNamedEntityFoundCounts = new Counter[Array[String]]()
  val badNamedEntityCorrectedCounts = new Counter[Array[String]]()
  val inputFiles = FileUtils.findFiles(inputDirName, ".txt.restored.out")

  Using.resource(new ConllFile(uncorrectedOutputFileName)) { uncorrectedConllFile =>
    Using.resource(new ConllFile(correctedOutputFileName)) { conllFile =>
      inputFiles.foreach { inputFile =>
        Using.resource(new InputFile(inputFile)) { inputFile =>
          while ({
            val (sentenceWords, sentenceEntities) = inputFile.getSentenceWordsAndEntities
            val lowerSentenceWords = sentenceWords.map(_.toLowerCase)
            val newEntities = sentenceEntities.clone
            val found = badNamedEntityWords.foldLeft(false) { (found, entityWords) =>
              val sliceIndex = lowerSentenceWords.indexOfSlice(entityWords)

              if (sliceIndex >= 0) {
                val range = Range(sliceIndex, sliceIndex + entityWords.length)

                badNamedEntityFoundCounts.incrementCount(entityWords)
                if (NamedEntity.isComplete(sentenceEntities, range)) {
                  badNamedEntityCorrectedCounts.incrementCount(entityWords)
                  range.foreach(newEntities(_) = NamedEntity.OUTSIDE)
                }
                true
              }
              else found
            }

            if (found) {
              conllFile.save(sentenceWords, newEntities)
              uncorrectedConllFile.save(sentenceWords, sentenceEntities)
            }
            sentenceWords.nonEmpty
          }) {}
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

class InputFile(file: File) extends AutoCloseable{
  protected val source: BufferedSource = Sourcer.sourceFromFile(file)
  protected val lines: Iterator[String] = source.getLines()

  def close(): Unit = source.close()

  def getSentenceWordsAndEntities: (Array[String], Array[String]) = {
    val sentenceLines = lines.takeWhile(_.nonEmpty).toArray
    val (words, entities) = {
      val parts = sentenceLines.map(_.split('\t'))
      val words = parts.map(_ (0))
      val entities = parts.map(_ (1))

      (words, entities)
    }

    (words, entities)
  }
}

class ConllFile(fileName: String) extends AutoCloseable {
  val printWriter: PrintWriter = {
    val printWriter = FileUtils.printWriterFromFile(new File(fileName))

    printWriter.print("-DOCSTART-\t0\n\t\n")
    printWriter
  }

  def close(): Unit = printWriter.close()

  def save(words: Array[String], entities: Array[String]): Unit = {
    words.zip(entities).foreach { case (word, entity) =>
      printWriter.print(s"$word\t$entity\n")
    }
    printWriter.print("\t\n")
  }
}

class NonEntitiesFile(fileName: String) {

  protected def loadNamedEntities(): Array[String] = {

    def isOdd(string: String): Boolean = string.count(_ == '"') % 2 != 0

    val fields = Using.resource(Sourcer.sourceFromFile(new File(fileName))) { source =>
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
      val fields = records.map(StringUtils.afterFirst(_, ',', all = false))

      fields
    }
    fields.toArray
  }

  protected def parseNamedEntities(fields: Array[String]): Array[Array[String]] = {
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


  def load(): Array[Array[String]] = {
    val badNamedEntities: Array[String] = loadNamedEntities()
    val badNamedEntityWords: Array[Array[String]] = parseNamedEntities(badNamedEntities)

    badNamedEntityWords
  }
}

object NamedEntity {
  val BEGIN = "B-"
  val INSIDE = "I-"
  val OUTSIDE = "O"
  def isComplete(bioLabels: Array[String], range: Range): Boolean = {
    val complete = bioLabels(range.head).startsWith(BEGIN) &&
        range.tail.forall(bioLabels(_).startsWith(INSIDE)) &&
        !bioLabels.lift(range.end).exists(_.startsWith(INSIDE))

    complete
  }
}