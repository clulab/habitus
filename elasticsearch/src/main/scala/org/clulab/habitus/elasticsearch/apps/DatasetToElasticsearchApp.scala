package org.clulab.habitus.elasticsearch.apps

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.cat.{IndicesRequest, IndicesResponse}
import co.elastic.clients.elasticsearch.core.{IndexRequest, IndexResponse}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, CreateIndexResponse, ElasticsearchIndicesClient}
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

import java.io.{File, FileInputStream}
import java.util.Properties
import scala.util.Using

case class CauseOrEffect(
  text: String,
  incCount: Int,
  decCount: Int,
  posCount: Int,
  negCount: Int
) {

}

case class Relation(cause: CauseOrEffect, effect: CauseOrEffect) {

}

case class CausalRelation(
  index: Int,
  negationCount: Int,
  relations: Seq[Relation]
) {

}

case class LatLon(lat: Float, lon: Float)

case class Location(
  val name: String,
  val latLon: LatLon
) {

}

// TODO: almost all of these are optional
case class DatasetRow(
  dataset: String,
  region: String,
  url: String,
  titleOpt: Option[String],
  terms: Seq[String],
  dateline: String,
  date: String,
  byline: String,
  sentenceIndex: Int,
  sentence: String,
  causalRelations: Seq[CausalRelation],
  isBelief: Boolean,
  sentiment: String,
  sentenceLocations: Seq[Location],
  contextBefore: String,
  contextAfter: String,
  contextLocations: Seq[Location],
  prevLocations: Seq[Location],
  prevDistance: Int,
  nextLocations: Seq[Location],
  nextDistance: Int
) {

}

class Credentials(filename: String) {
  val (username, password) = Using.resource(new FileInputStream(filename)) { inputStream =>
    val properties = {
      val properties = new Properties()

      properties.load(inputStream)
      properties
    }
    val username = properties.getProperty("username")
    val password = properties.getProperty("password")

    (username, password)
  }
}

object DatasetToElasticsearchApp extends App {
  println(new File("something").getAbsolutePath())
  val credentialsFilename = "../../credentials/credentials.properties"
  val indexName = "habitus4"

  def mkCredentialsProvider(credentialsFilename: String) = {
    val credentials = new Credentials(credentialsFilename)
    val credentialsProvider = {
      val usernamePasswordCredentials = new UsernamePasswordCredentials(credentials.username, credentials.password)
      val credentialsProvider = new BasicCredentialsProvider()

      credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials)
      credentialsProvider
    }

    credentialsProvider
  }

  def mkRestClient(credentialsFilename: String): RestClient = {
    val restClient = RestClient
        .builder(new HttpHost("localhost", 9200))
        // .builder(new HttpHost("elasticsearch.keithalcock.com", 443, "https"))
        .setHttpClientConfigCallback(new HttpClientConfigCallback() {
          def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
            httpClientBuilder.setDefaultCredentialsProvider(mkCredentialsProvider(credentialsFilename))
          }
        })
        .build()

    restClient
  }

  def mkElasticsearchClient(credentialsFilename: String): ElasticsearchClient = {
    val restClient = mkRestClient(credentialsFilename)
    val jsonpMapper = new JacksonJsonpMapper()
    val elasticsearchTransport = new RestClientTransport(restClient, jsonpMapper)
    val elasticsearchClient = new ElasticsearchClient(elasticsearchTransport)

    elasticsearchClient
  }

  def runCreateIndex(elasticsearchClient: ElasticsearchClient, indexName: String): CreateIndexResponse = {
    val elasticsearchIndicesClient = elasticsearchClient.indices()
    val createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build()
    val createIndexResponse = elasticsearchIndicesClient.create(createIndexRequest)

    println(createIndexResponse)
    createIndexResponse
  }

  def runIndex(elasticsearchClient: ElasticsearchClient, indexName: String, datasetRow: DatasetRow): IndexResponse = {
    val indexRequest = new IndexRequest.Builder().index(indexName).document(datasetRow).build()
    val indexResponse = elasticsearchClient.index(indexRequest)
    println(indexResponse)

    indexResponse
  }

  def runIndices(elasticsearchClient: ElasticsearchClient): IndicesResponse = {
    val elasticsearchCatClient = elasticsearchClient.cat()
    val indicesResponse = elasticsearchCatClient.indices()

    println(indicesResponse)
    indicesResponse
  }

  def run(): Unit = {
    val elasticsearchClient = mkElasticsearchClient(credentialsFilename)
//    val datasetRow = DatasetRow(
//
//    )

    // runCreateIndex(elasticsearchClient, indexName)
    runIndices(elasticsearchClient)
    //runIndex(elasticsearchClient, indexName, datasetRow)

    println("Goodbye")
  }

  try {
    run()
  }
  catch {
    case throwable: Throwable =>
      println(throwable)
  }
}
