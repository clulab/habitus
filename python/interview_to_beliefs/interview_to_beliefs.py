import faiss
import pandas

from sentence_transformers import SentenceTransformer

if __name__ == "__main__":
    base_dir_name = "../../../corpora/"
    # beliefs_path_name = sys.argv[1]
    beliefs_path_name = base_dir_name + "beliefs_with_metadata_by_conf_score/senegal_hr_queries_beliefs_80conf_with_metadata.tsv"
    # interview_path_name = sys.argv[2]
    interview_path_name = base_dir_name + "experiment_corpus/row_labels_harvest.csv"
    encoding = "utf-8"
    k = 5

    beliefs_dataframe = pandas.read_table(beliefs_path_name, index_col = 0, header = 0, encoding = encoding)
    beliefs = beliefs_dataframe["belief"].tolist()

    interview_dataframe = pandas.read_csv(interview_path_name, index_col = 0, header = 0, encoding = encoding)
    interview_sentences = interview_dataframe["readable"].tolist()

    model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
    # These embeddings appear to be normalized.
    beliefs_embeddings = model.encode(beliefs)
    interview_sentences_embeddings = model.encode(interview_sentences)

    width = beliefs_embeddings.shape[1]
    # index = faiss.IndexFlatL2(width) # This is for 2-D Euclidean distance.
    index = faiss.IndexFlatIP(width) # IP is inner product, so cosine similarity given normalized vectors.
    index.add(beliefs_embeddings)
    assert index.is_trained

    _, beliefs_indexes_collection = index.search(interview_sentences_embeddings, k)

    for index, beliefs_indexes in enumerate(beliefs_indexes_collection):
        interview_sentence = interview_sentences[index]
        print(interview_sentence)
        for beliefs_index in beliefs_indexes:
            print("\t" + beliefs[beliefs_index])
