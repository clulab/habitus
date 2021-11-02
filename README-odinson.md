# Habitus Odinson framework

The `org.clulab.odinson` package includes a simple framework for using Odinson.

This code uses the latest snapshot of Odinson.
Please clone the odinson repository and publish it to your local ivy repository.

    git clone git@github.com:lum-ai/odinson.git
    cd odinson
    sbt publishLocal

## Setting up the corpus

First we need to configure the desired location of our data folder in `application.conf`.
By default, it is `~/data/habitus`. In that directory, there should be another dir called
`docs` and it should store all the documents in the corpus as Odinson documents.

To index the odinson documents:

    sbt 'runMain ai.lum.odinson.extra.IndexDocuments'

If you only have text files, make a directory called `text` in the datadir with your
text files and then call

    sbt 'runMain ai.lum.odinson.extra.AnnotateText'

This command will populate the `docs` directory, and then you can build the index.

## Running the project

To run the rules type

    sbt 'runMain org.clulab.odinson.Main'

The output mentions will be stored in the `mentions.json` file, as specified in `application.conf`.

