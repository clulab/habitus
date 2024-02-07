package org.clulab.habitus.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.{IndexRequest, IndexResponse}
import co.elastic.clients.elasticsearch.indices.{CreateIndexRequest, CreateIndexResponse}
import co.elastic.clients.json.JsonData
import org.clulab.habitus.elasticsearch.data.DatasetRecord
import org.clulab.habitus.elasticsearch.utils.Elasticsearch
import org.elasticsearch.client.RestClient

import java.net.URL

class ElasticsearchIndexClient(val indexName: String, elasticsearchClient: ElasticsearchClient) {

  def createIndex(): CreateIndexResponse = {
    val elasticsearchIndicesClient = elasticsearchClient.indices()
    val createIndexRequest = new CreateIndexRequest.Builder().index(indexName).build()
    val createIndexResponse = elasticsearchIndicesClient.create(createIndexRequest)

    println(createIndexResponse)
    createIndexResponse
  }

  def index(datasetRecord: DatasetRecord): IndexResponse = {
    val indexRequest = new IndexRequest.Builder()
        .index(indexName)
        .document(JsonData.of(datasetRecord.serialize))
        .build()
    val indexResponse = elasticsearchClient.index(indexRequest)
    println(indexResponse)

    indexResponse
  }
}

object ElasticsearchIndexClient {

  def apply(restClient: RestClient, indexName: String): ElasticsearchIndexClient = {
    val elasticsearchClient = Elasticsearch.mkElasticsearchClient(restClient)

    new ElasticsearchIndexClient(indexName, elasticsearchClient)
  }

  def apply(url: URL, credentialsFilename: String, indexName: String): ElasticsearchIndexClient = {
    val restClient = Elasticsearch.mkRestClient(url, credentialsFilename)

    apply(restClient, indexName)
  }
}
