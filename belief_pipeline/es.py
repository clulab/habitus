from elasticsearch import Elasticsearch

client = Elasticsearch(
  "http://localhost:9200/",
  basic_auth=("user", "password")
)

# print(client.info())

result = client.search(index="habitus3", q="Karamoja")

print(result)
