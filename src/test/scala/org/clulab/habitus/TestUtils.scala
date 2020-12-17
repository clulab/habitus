package org.clulab.habitus

import com.typesafe.config.Config
import org.scalatest._
import ai.lum.odinson._

object TestUtils {

  def getDocumentFromJson(json: String): Document = Document.fromJson(json)
  def getDocument(id: String): Document = getDocumentFromJson(ExampleDocs.json(id))

  def mkExtractorEngine(doc: Document): ExtractorEngine = {
    ExtractorEngine.inMemory(doc)
  }

  def mkExtractorEngine(config: Config, doc: Document): ExtractorEngine = {
    ExtractorEngine.inMemory(config, Seq(doc))
  }

}
