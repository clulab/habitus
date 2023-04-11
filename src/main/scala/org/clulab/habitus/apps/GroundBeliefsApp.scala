package org.clulab.habitus.apps

import org.clulab.embeddings.{WordEmbeddingMap, WordEmbeddingMapPool}
import org.clulab.processors.clu.CluProcessor
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader
import zamblauskas.csv.parser._
import zamblauskas.functional._

import java.io.File

case class GridDocument(
  readable: String,
  stripped: String
) {
  val index: Int = GridIndex.nextInt
}

object GridIndex {
  protected var value: Int = 0

  def nextInt: Int = {
    val result = value

    value += 1
    result
  }
}

case class Belief(
  index: Int,
  belief: String,
  title: String,
  author: String,
  year: String
)

object GroundBeliefsApp extends App {
  implicit val gridDocumentReads: ColumnReads[GridDocument] = (
    // column("index").as[Int] and // For some reason, this doesn't work.
    column("readable").as[String] and
    column("stripped").as[String]
  )(GridDocument)

  val documentsFileName = args.lift(0).getOrElse("../grounding/training_docs.csv")
  val beliefsFileName = args.lift(1).getOrElse("../grounding/beliefs.tsv")
  val groundingFileName = args.lift(2).getOrElse("../grounding/training_beliefs.tsv")
  val processor = new CluProcessor()
  val wordEmbeddingMap = WordEmbeddingMapPool.getOrElseCreate("/org/clulab/glove/glove.840B.300d.10f", compact = true)

  def calcGrounding(text: String): Array[Float] = {
    // TODO: Make this more selective
    val words = processor.mkDocument(text).sentences.flatMap(_.words)
    val grounding = wordEmbeddingMap.makeCompositeVector(words)

    grounding
  }

  def calcDistance(leftGrounding: Array[Float], rightGrounding: Array[Float]): Float = {
    val distance = WordEmbeddingMap.dotProduct(leftGrounding, rightGrounding)

    distance
  }

  val documents: Seq[GridDocument] = {
    val text = FileUtils.getTextFromFile(documentsFileName)
    val result = Parser.parse[GridDocument](text)
    val documents = result.toOption.get

    documents
  }
  val beliefs: Seq[Belief] = {
    val tsvReader = new TsvReader()
    val beliefs = Sourcer.sourceFromFile(new File(beliefsFileName)).autoClose { source =>
      source.getLines().drop(1).map { line =>
        val Array(index, belief, title, author, year) = tsvReader.readln(line, 5)

        Belief(index.toInt, belief, title, author, year)
      }.toVector
    }

    beliefs
  }
  val documentGroundings = documents.map { document => calcGrounding(document.readable) }
  val beliefGroundings = beliefs.map { belief => calcGrounding(belief.belief) }

  FileUtils.printWriterFromFile(groundingFileName).autoClose { printWriter =>
    documentGroundings.map { case documentGrounding =>
      val beliefIndexAndDistanceTuples = beliefGroundings.zipWithIndex.map { case (beliefGrounding, beliefIndex) =>
        val distance = calcDistance(documentGrounding, beliefGrounding)

        (beliefIndex, distance)
      }
      val sortedBeliefIndexAndDistanceTuples = beliefIndexAndDistanceTuples.sortBy(-_._2)
      val sortedBeliefIndexes = sortedBeliefIndexAndDistanceTuples.map(_._1)

      printWriter.println(sortedBeliefIndexes.mkString("\t"))
    }
  }
}
