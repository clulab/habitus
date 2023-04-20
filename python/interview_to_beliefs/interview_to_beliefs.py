import faiss
import pandas

from sentence_transformers import SentenceTransformer

if __name__ == "__main__":
    beliefs_file = "../../../corpora/beliefs_with_metadata_by_conf_score/senegal_hr_queries_beliefs_80conf_with_metadata.tsv" # sys.argv[1]
    interviews_file = "../../../corpora/experiment_corpus/row_labels_harvest.csv" # sys.argv[2]
    encoding = "utf-8"
    k = 5

    beliefs_dataframe = pandas.read_table(beliefs_file, index_col = 0, header = 0, encoding = encoding)
    beliefs = beliefs_dataframe["belief"].tolist()

    interviews_dataframe = pandas.read_csv(interviews_file, index_col = 0, header = 0, encoding = encoding)
    interviews = interviews_dataframe["readable"].tolist()

    model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
    beliefs_embeddings = model.encode(beliefs)
    interviews_embeddings = model.encode(interviews)

    width = beliefs_embeddings.shape[1]
    index = faiss.IndexFlatL2(width)
    index.add(beliefs_embeddings)
    print(index.is_trained)

    _, beliefs_indexes_list = index.search(interviews_embeddings, k)

    for index, beliefs_indexes in enumerate(beliefs_indexes_list):
        interview = interviews[index]
        print(interview)
        for beliefs_index in beliefs_indexes:
            print("\t" + beliefs[beliefs_index])
