package org.clulab.habitus.elasticsearch.apps

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.{IdsQuery, Query}
import co.elastic.clients.elasticsearch.cat.{IndicesRequest, IndicesResponse}
import co.elastic.clients.elasticsearch.core.{IndexRequest, IndexResponse, SearchRequest, SearchResponse}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, CreateIndexResponse, ElasticsearchIndicesClient}
import co.elastic.clients.json.{JsonData, JsonpMapper, JsonpSerializer}
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import jakarta.json.stream.JsonGenerator
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.{Request, RestClient}
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

import java.io.{File, FileInputStream}
import java.util.HashMap
import java.util.Properties
import scala.util.Using

case class CauseOrEffect(
  text: String,
  incCount: Int,
  decCount: Int,
  posCount: Int,
  negCount: Int
) {

  def serialize(): HashMap[String, AnyRef] = {
    ???
  }
}

case class Relation(cause: CauseOrEffect, effect: CauseOrEffect) {

  def serialize(): HashMap[String, AnyRef] = {
    ???
  }
}

case class CausalRelation(
  index: Int,
  negationCount: Int,
  relations: Array[Relation]
) {

  def serialize(): HashMap[String, AnyRef] = {
    ???
  }
}

case class LatLon(lat: Float, lon: Float) {

  def serialize(): HashMap[String, AnyRef] = {
    ???
  }
}

case class Location(
  val name: String,
  val latLon: LatLon
) {

  def serialize(): HashMap[String, AnyRef] = {
    ???
  }
}

// TODO: almost all of these are optional
case class DatasetRecord(
  dataset: String,
  region: String,
  url: String,
  title: String,
  terms: Array[String],
  dateline: String,
  date: String,
  byline: String,
  sentenceIndex: Int,
  sentence: String,
  causalRelations: Array[CausalRelation],
  isBelief: Boolean,
  sentiment: String,
  sentenceLocations: Array[Location],
  contextBefore: String,
  contextAfter: String,
  contextLocations: Array[Location],
  prevLocations: Array[Location],
  prevDistance: Int,
  nextLocations: Array[Location],
  nextDistance: Int
) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("dataset", dataset)
    map.put("region", region)
    map.put("url", url)
    map.put("title", title)
    map
  }
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
  val indexName = "habitus"

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

  def mkElasticsearchTransport(restClient: RestClient): ElasticsearchTransport = {
    val jsonpMapper = new JacksonJsonpMapper()
    val elasticsearchTransport = new RestClientTransport(restClient, jsonpMapper)

    elasticsearchTransport
  }

  def mkElasticsearchClient(restClient: RestClient): ElasticsearchClient = {
    val elasticsearchTransport = mkElasticsearchTransport(restClient)
    val elasticsearchClient = new ElasticsearchClient(elasticsearchTransport)

    elasticsearchClient
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

  def runCreateIndex(elasticsearchClient: ElasticsearchClient, indexName: String): CreateIndexResponse = {
    val elasticsearchIndicesClient = elasticsearchClient.indices()
    val createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build()
    val createIndexResponse = elasticsearchIndicesClient.create(createIndexRequest)

    println(createIndexResponse)
    createIndexResponse
  }

  def runIndex(elasticsearchClient: ElasticsearchClient, indexName: String, datasetRecord: DatasetRecord): IndexResponse = {
    val indexRequest = new IndexRequest.Builder()
        .index(indexName)
        // .withJson()
        // .document(datasetRecord)
        // .document(JsonData.of(datasetRecord)) try this
        .document(JsonData.of(datasetRecord.serialize))
        // .tDocumentSerializer(new DatasetRecordSerializer)
        .build()
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

  def mkDatasetRow(): DatasetRecord = {
    val datasetRow = DatasetRecord(
      dataset = "uganda.tsv",
      region = "uganda",
      url = "http://clulab.org",
      title = "This article is about that",
      terms = Array("mining", "galamsey"),
      dateline = "September 2, 2023",
      date = "2023-09-02",
      byline = "Your local reporter",
      sentenceIndex = 0,
      sentence = "The quick brown fox jumped over the lazy dog.",
      causalRelations = Array(
        CausalRelation(
          index = 1,
          negationCount = 1,
          relations = Array(
            Relation(
              cause = CauseOrEffect(
                text = "cause1-1",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect1-1",
                incCount = 2,
                decCount = 3,
                posCount = 4,
                negCount = 5
              )
            ),
            Relation(
              cause = CauseOrEffect(
                text = "cause1-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect1-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              )
            )
          )
        ),
        CausalRelation(
          index = 2,
          negationCount = 1,
          relations = Array(
            Relation(
              cause = CauseOrEffect(
                text = "cause2-1",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect2-1",
                incCount = 2,
                decCount = 3,
                posCount = 4,
                negCount = 5
              )
            ),
            Relation(
              cause = CauseOrEffect(
                text = "cause2-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect2-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              )
            )
          )
        )
      ),
      isBelief = true,
      sentiment = "POSITIVE",
      sentenceLocations = Array(
        Location(
          name = "sentenceLocation1",
          latLon = LatLon(1f, 2f)
        ),
        Location(
          name = "sentenceLocation2",
          latLon = LatLon(3f, 4f)
        )
      ),
      contextBefore = "This is what came before",
      contextAfter = "This is what came after",
      contextLocations = Array(
        Location(
          name = "contextLocation1",
          latLon = LatLon(5f, 6f)
        ),
        Location(
          name = "contextLocation2",
          latLon = LatLon(7f, 8f)
        )
      ),
      prevLocations = Array(
        Location(
          name = "prevLocation1",
          latLon = LatLon(9f, 10f)
        ),
        Location(
          name = "prevLocation2",
          latLon = LatLon(11f, 12f)
        )
      ),
      prevDistance = 4,
      nextLocations = Array(
        Location(
          name = "nextLocation1",
          latLon = LatLon(13f, 14f)
        ),
        Location(
          name = "nextLocation2",
          latLon = LatLon(15f, 16f)
        )
      ),
      nextDistance = 5
    )

    datasetRow
  }

  def runSearch(elasticsearchClient: ElasticsearchClient, indexName: String, docId: String): String = {
    val idsQuery: IdsQuery = new IdsQuery.Builder().values(docId).build()
    val query: Query = new Query.Builder().ids(idsQuery).build() // What does this have to look like?
    val searchRequest = new SearchRequest.Builder().index(indexName).query(query).build()
    val searchResponse = elasticsearchClient.search(searchRequest, classOf[DatasetRecord])
    val searchResponseString = searchResponse.toString

    println(searchResponseString)
    searchResponseString
  }

  def runSearchLow(restClient: RestClient, indexName: String): String = {
    val request = new Request("GET", indexName)
    request.addParameter("pretty", "true")
    val response = restClient.performRequest(request)
    val statusCode = response.getStatusLine.getStatusCode
    val responseBody = EntityUtils.toString(response.getEntity)

    println(responseBody)
    responseBody
  }

  def run(restClient: RestClient): Unit = {
    val elasticsearchClient = mkElasticsearchClient(restClient)

    // runCreateIndex(elasticsearchClient, indexName)
    // runIndices(elasticsearchClient)
    runIndex(elasticsearchClient, indexName, mkDatasetRow())
    // runSearch(elasticsearchClient, indexName, "pydJYo0BaDron0AvRCXx")
    // runSearchLow(restClient, indexName)
  }

  try {
    Using.resource(mkRestClient(credentialsFilename)) { restClient =>
      run(restClient)
    }
    println("Goodbye")
  }
  catch {
    case throwable: Throwable =>
      println(throwable)
  }
}
