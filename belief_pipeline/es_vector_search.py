from argparse import ArgumentParser
from elasticsearch import Elasticsearch
from sentence_transformers import SentenceTransformer

class VectorSearcher():
    def __init__(self, url, username, password, model_name):
        super().__init__()
        self.elasticsearch = Elasticsearch(url, basic_auth=(username, password))
        self.sentence_transformer = SentenceTransformer(model_name)

    def search(self, k, text):
        index = "habitus2"
        # This vector is assumed to be normalized.
        vector = self.sentence_transformer.encode(text).tolist()
        query = {
            "field": "chatVector",
            "query_vector": vector,
            "k": k,
            "num_candidates": k
        }
        result = self.elasticsearch.search(index=index, knn=query, source=False)
        hits = result.body["hits"]["hits"]
        ids_and_scores = [(hit["_id"], hit["_score"]) for hit in hits]
        print(result)
        return ids_and_scores

def run(username, password, k, text):
    url = "http://localhost:9200/"
    model_name = "all-MiniLM-L6-v2"
    vector_searcher = VectorSearcher(url, username, password, model_name)
    ids_and_scores = vector_searcher.search(k, text)
    print(ids_and_scores)

def get_args():
    argument_parser = ArgumentParser()
    argument_parser.add_argument("-u", "--username", required=True, help="elasticsearch username")
    argument_parser.add_argument("-p", "--password", required=True, help="elasticsearch password")
    argument_parser.add_argument("-k", "--k",        required=True, help="number of nearest neighbors")
    argument_parser.add_argument("-t", "--text",     required=True, help="text to be matched")
    args = argument_parser.parse_args()
    return args.username, args.password, args.k, args.text

if __name__ == "__main__":
    username, password, k, text = get_args()
    run(username, password, k, text)
