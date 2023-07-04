# Create a virtual environment for Python and activate it.
python3.9 -m venv venv
source ./venv/bin/activate

# classify/Belief
pip install datasets
pip install transformers
pip install torch
pip install transformers[torch]

# the rest of these come free with the above
pip install pandas
pip install nltk
python -c 'import nltk; nltk.download("stopwords")'
# This below didn't seem to do anything.
python -c 'from transformers import AutoTokenizer; model = AutoTokenizer.from_pretrained("bert-base-cased", num_labels=2)'
# How to download Masha's model?

# rank/Similarity
pip install sentence-transformers
python -c 'from sentence_transformers import SentenceTransformer; SentenceTransformer("all-mpnet-base-v2")'

# locate/Location
pip install spacy
python -m spacy download en_core_web_sm

# Sentiment 
pip install transformers # already done above
#pip install datasets     # ditto
#pip install accelerate


