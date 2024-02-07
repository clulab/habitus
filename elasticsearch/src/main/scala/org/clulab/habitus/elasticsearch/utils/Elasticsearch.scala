package org.clulab.habitus.elasticsearch.utils

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.{IdsQuery, Query}
import co.elastic.clients.elasticsearch.cat.IndicesResponse
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.util.EntityUtils
import org.clulab.habitus.elasticsearch.data.DatasetRecord
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.{Request, RestClient}

import java.net.URL

object Elasticsearch {

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

  def mkRestClient(url: URL, credentialsFilename: String): RestClient = {
    val host = url.getHost
    val port = url.getPort // -1 if none
    val protocol = url.getProtocol
    val restClient = RestClient
      .builder(new HttpHost(host, port, protocol))
      .setHttpClientConfigCallback(new HttpClientConfigCallback() {
        def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
          httpClientBuilder.setDefaultCredentialsProvider(mkCredentialsProvider(credentialsFilename))
        }
      })
      .build()

    restClient
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
    val request = new Request("GET", s"/$indexName/_search")
    request.addParameter("pretty", "true")
    val response = restClient.performRequest(request)
    val statusCode = response.getStatusLine.getStatusCode
    val responseBody = EntityUtils.toString(response.getEntity)

    println(responseBody)
    responseBody
  }
}
