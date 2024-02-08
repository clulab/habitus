package org.clulab.habitus.elasticsearch.apps

import org.clulab.habitus.elasticsearch.ElasticsearchIndexClient
import org.clulab.habitus.elasticsearch.data.DatasetRecord
import org.clulab.habitus.elasticsearch.utils.Elasticsearch
import org.elasticsearch.client.RestClient

import java.io.File
import java.net.URL
import scala.util.Using

object DatasetToElasticsearchApp extends App {
  val credentialsFilename = "../../credentials/elasticsearch-credentials.properties"
  val indexName = "habitus"
  val url = new URL("http://localhost:9200")
  // val url = new URL("https://elasticsearch.keithalcock.com")

  def run(restClient: RestClient): Unit = {
    val elasticsearchClient = Elasticsearch.mkElasticsearchClient(restClient)
    val elasticsearchIndexClient = new ElasticsearchIndexClient(indexName, elasticsearchClient)

    elasticsearchIndexClient.createIndex()
    elasticsearchIndexClient.index(DatasetRecord.default)

    // Elasticsearch.runIndices(elasticsearchClient)
    // Elasticsearch.runSearch(elasticsearchClient, indexName, "pydJYo0BaDron0AvRCXx")
    // Elasticsearch.runSearchLow(restClient, indexName)
  }

  try {
    Using.resource(Elasticsearch.mkRestClient(url, credentialsFilename)) { restClient =>
      run(restClient)
    }
    println("Goodbye")
  }
  catch {
    case throwable: Throwable =>
      println(throwable)
  }
}
