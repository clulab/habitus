# belief_pipeline

## Introduction

The pipeline incorporates code from Masha's [iBelieve](https://github.com/maxaalexeeva/iBelieve) project, specifically the

* Jupyter notebooks
    * [classifying_beliefs_and_writing_to_tsv_wassa_ghana_web_jun7.ipynb](https://github.com/maxaalexeeva/iBelieve/blob/main/belief_ranking/classifying_beliefs_and_writing_to_tsv_wassa_ghana_web_jun7.ipynb)
    * [just_belief_similarity_ranking_and_writing_to_file_cleanup_june2.ipynb](https://github.com/maxaalexeeva/iBelieve/blob/main/belief_ranking/just_belief_similarity_ranking_and_writing_to_file_cleanup_june2.ipynb)
    * [add_locations_to_beliefs.ipynb](https://github.com/maxaalexeeva/iBelieve/blob/main/belief_ranking/add_locations_to_beliefs.ipynb) from Masha and
    * [beliefs_sentiment_analysis](https://github.com/maxaalexeeva/iBelieve/blob/main/belief_sentiments/beliefs_sentiment_analysis.ipynb) from Haris, along with

* models
    * [belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34](https://huggingface.co/maxaalexeeva/belief-classifier_mturk_unmarked-trigger_bert-base-cased_2023-4-26-0-34) from Masha and
    * [finetuned_beliefs_sentiment_classifier_experiment1](https://huggingface.co/hriaz/finetuned_beliefs_sentiment_classifier_experiment1) from Haris

## Preparation

Quite a few Python packages, huggingface models, and torch transformers are required for this to work.  A Python virtual environment is highly recommended.  Although the models and transformers can be downloaded by the program, this means that some runs will take substantially longer than others.  It is recommended that all external files be downloaded in advance.  Once this has been done, no more internet access should be required to run the program.  The file `prepare.sh` contains example instructions that can be copied and pasted to the command line in order to set up a working system.

## Usage

After everything is installed and there are text files in the input directory, run 

```
python belief_pipeline/main.py -i <input_directory_name> -o <output_file_name>`
```
