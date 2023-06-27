# classify/Belief
pip install pandas
pip install torch
pip install nltk
pip install datasets
pip install transformers
pip install transformers[torch]
python -c 'import nltk; nltk.download("stopwords")'
python -c 'from transformers import AutoTokenizer; AutoTokenizer.from_pretrained("bert-base-cased")'
# How to download Masha's model?

# rank/Similarity
pip install sentence-transformers
python -c 'from sentence_transformers import SentenceTransformer; SentenceTransformer("all-mpnet-base-v2")'

# locate/Location
pip install spacy
python -m spacy download en_core_web_sm

# Sentiment 
# How to download Haris models?
