package org.clulab.habitus.apps.lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, TopScoreDocCollector}
import org.apache.lucene.store.FSDirectory

import java.nio.file.Paths

object LuceneSearcherApp extends App {
  val luceneDirname = "../lucene"
  val field = "text"

  def newReader(): DirectoryReader = {
    val path = Paths.get(luceneDirname)
    val fsDirectory = FSDirectory.open(path)

    DirectoryReader.open(fsDirectory)
  }

  def newQuery(query: String): Query = {
    val analyzer = new StandardAnalyzer()

    new QueryParser(field, analyzer).parse(query)
  }

  def documentSearch(queryString: String, maxHits: Int): Iterator[(Float, Document)] = {
    val collector = TopScoreDocCollector.create(maxHits)
    val query = newQuery(queryString)
    val searcher = new IndexSearcher(reader)

    searcher.search(query, collector)

    val scoreDocs = collector.topDocs().scoreDocs
    val scoresAndDocs = scoreDocs.map { hit => (hit.score, searcher.doc(hit.doc)) }

    scoresAndDocs.iterator
  }

  val reader = newReader()
  val results = documentSearch("karamoja", 10)

  results.foreach { case (score, document) =>
    val url = document.get("url")
    val year = document.get("year")

    println(s"$score\t$year\t$url")
  }
}
