# Clone the repo.
git clone https://github.com/clulab/habitus
cd habitus

# Create a virtual environment for Python and activate it.
python3.9 -m venv venv
source ./venv/bin/activate

# classify/Belief
pip install datasets
pip install transformers
pip install torch
pip install transformers[torch]
# These may come free with the above as transitive dependencies.
pip install pandas
pip install nltk
# This will be downloaded to a cache, ~/nltk_data/corpora/stopwords.zip and unzipped there.
python -c 'import nltk; nltk.download("stopwords")'
# This will be downloaded to a cache, ~/.cache/huggingface/hub/models--bert-base-cased.
python -c 'from transformers import AutoTokenizer; model = AutoTokenizer.from_pretrained("bert-base-cased", num_labels=2)'
# This will be downloaded to a cache, ~/.cache/huggingface/hub/models--maxaalexeeva...
python -c 'from huggingface_hub import snapshot_download; snapshot_download(repo_id="maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34")'

# rank/Similarity
pip install sentence-transformers
# This will be downloaded to a cache, ~/.cache/torch/sentence_transformers
python -c 'from sentence_transformers import SentenceTransformer; SentenceTransformer("all-mpnet-base-v2")'

# locate/Location
pip install spacy
# This will be downloaded to the site-packages directory of the Python venv, venv/lib/pythonX/site-packages.
python -m spacy download en_core_web_sm
# This list of locations in Ghana is now included in the github repo.

# Sentiment 
pip install transformers # already done above
# This will be downloaded to a cache, ~/.cache/huggingface/hub/models--hriaz...
python -c 'from huggingface_hub import snapshot_download; snapshot_download(repo_id="hriaz/finetuned_beliefs_sentiment_classifier_experiment1")'

# Run the program
python belief_pipeline/main.py -i ../corpora/Galamsey-web-435 -o ../sample_output.tsv