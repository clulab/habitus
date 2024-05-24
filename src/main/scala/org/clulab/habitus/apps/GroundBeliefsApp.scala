package org.clulab.habitus.apps

import org.clulab.embeddings.{WordEmbeddingMap, WordEmbeddingMapPool}
import org.clulab.habitus.utils.TsvReader
import org.clulab.processors.clu.CluProcessor
import org.clulab.utils.{FileUtils, Sourcer}
import org.clulab.wm.eidoscommon.EnglishTagSet
import zamblauskas.csv.parser._
import zamblauskas.functional._

import java.io.File
import scala.util.Using

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
  val tagSet = new EnglishTagSet()

  def isCanonical(lemma: String, tag: String, ner: String): Boolean = {
    tagSet.isOntologyContent(tag) // &&
//      !stopwordManaging.containsStopwordStrict(lemma) &&
//      !stopwordManaging.containsStopwordNer(ner)
  }

  def calcGrounding(text: String): Array[Float] = {
    val document = {
      val document = processor.mkDocument(text)

      processor.annotate(document)
      document
    }
    val words = document.sentences.flatMap { sentence =>
      val words = sentence.words
      val lemmas = sentence.lemmas.get
      val tags = sentence.tags.get
      val ners = sentence.entities.get
      val canonicalIndices = words.indices.filter { index =>
        isCanonical(lemmas(index), tags(index), ners(index))
      }
      val canonicalWords =
          if (canonicalIndices.nonEmpty) canonicalIndices.map(words).toArray
          else words

      canonicalWords
    }
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
    val beliefs = Using.resource(Sourcer.sourceFromFile(new File(beliefsFileName))) { source =>
      source.getLines().drop(1).map { line =>
        val Array(index, belief, title, author, year) = tsvReader.readln(line, 5)

        Belief(index.toInt, belief, title, author, year)
      }.toVector
    }

    beliefs
  }
  val documentGroundings = documents.zipWithIndex.map { case (document, index) =>
    println(s"Grounding document $index.")
    calcGrounding(document.readable)
  }.toArray
  val beliefGroundings = beliefs.zipWithIndex.map { case (belief, index) =>
    println(s"Grounding belief $index.")
    calcGrounding(belief.belief)
  }.toArray

  Using.resource(FileUtils.printWriterFromFile(groundingFileName)) { printWriter =>
    documentGroundings.map { case documentGrounding =>
      val beliefIndexAndDistanceTuples = beliefGroundings.zipWithIndex.map { case (beliefGrounding, beliefIndex) =>
        val distance = calcDistance(documentGrounding, beliefGrounding)

        (beliefIndex, distance)
      }
      val sortedBeliefIndexAndDistanceTuples = beliefIndexAndDistanceTuples.sortBy(-_._2)
      val sortedBeliefIndexes = sortedBeliefIndexAndDistanceTuples.map(_._1)

      println(sortedBeliefIndexAndDistanceTuples.mkString(", "))
      printWriter.println(sortedBeliefIndexes.mkString("\t"))
    }
  }
}
