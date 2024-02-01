package org.clulab.habitus.elasticsearch.apps

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.{IdsQuery, Query}
import co.elastic.clients.elasticsearch.cat.{IndicesRequest, IndicesResponse}
import co.elastic.clients.elasticsearch.core.{IndexRequest, IndexResponse, SearchRequest, SearchResponse}
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, CreateIndexResponse, ElasticsearchIndicesClient}
import co.elastic.clients.json.JsonData
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.util.EntityUtils
import org.clulab.habitus.elasticsearch.data.{CausalRelation, CauseOrEffect, DatasetRecord, LatLon, Location, Relation}
import org.clulab.habitus.elasticsearch.utils.Credentials
import org.elasticsearch.client.{Request, RestClient}
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

import java.io.File
import scala.util.Using

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
        .document(JsonData.of(datasetRecord.serialize))
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
    runIndex(elasticsearchClient, indexName, DatasetRecord.default)
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
