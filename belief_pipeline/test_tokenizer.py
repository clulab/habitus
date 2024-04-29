from sentence_transformers import SentenceTransformer
from transformers import AutoModel, AutoTokenizer

import torch
import torch.nn.functional as F

transformer = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")
tokenizer = AutoTokenizer.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")
model = AutoModel.from_pretrained("sentence-transformers/all-MiniLM-L6-v2")
sentence = "This is a test."

def mean_pooling(model_output, attention_mask):
    token_embeddings = model_output[0]
    input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
    return torch.sum(token_embeddings * input_mask_expanded, 1) / torch.clamp(input_mask_expanded.sum(1), min=1e-9)

def method1(sentence):
    sentence_embeddings = transformer.encode(sentence)
    # print(sentence_embeddings)
    return sentence_embeddings

def method2(sentence):
    encoded_input = tokenizer(sentence, padding=True, truncation=True, return_tensors="pt")
    print(encoded_input)

    # Compute token embeddings
    with torch.no_grad():
        model_output = model(**encoded_input)
    print(model_output)

    # Perform pooling
    sentence_embeddings = mean_pooling(model_output, encoded_input["attention_mask"])

    # Normalize embeddings
    sentence_embeddings = F.normalize(sentence_embeddings, p=2, dim=1)
    # print(sentence_embeddings)
    return sentence_embeddings

sentence_embeddings1 = method1(sentence)
sentence_embeddings2 = method2(sentence)[0]

for i in range(0, len(sentence_embeddings1)):
    print(sentence_embeddings1[i], sentence_embeddings2[i])

