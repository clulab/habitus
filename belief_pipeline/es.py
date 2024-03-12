from elasticsearch import Elasticsearch

client = Elasticsearch(
  "https://elasticsearch.habitus.clulab.org/",
  basic_auth=("user", "password")
  # api_key="..."
)

# print(client.info())

result = client.search(index="habitus3", q="Karamoja")

print(result)
