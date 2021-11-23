
# habitus

We're using the latest snapshot of Odinson.
Please clone the odinson repository and publish it to your local ivy repository.

    git clone git@github.com:lum-ai/odinson.git
    cd odinson
    sbt publishLocal

## setting up the corpus

First we need to configure the desired location of our data folder in `application.conf`.
By default, it is `~/data/habitus`. In that directory, there should be another dir called
`docs` and it should store all the documents in the corpus as Odinson documents.

To index the odinson documents:

    sbt 'runMain ai.lum.odinson.extra.IndexDocuments'

If you only have text files, make a directory called `text` in the datadir with your
text files and then call

    sbt 'runMain ai.lum.odinson.extra.AnnotateText'

This command will populate the `docs` directory, and then you can build the index.

## running the project

To run the rules type

    sbt 'runMain org.clulab.habitus.Main'

The output mentions will be stored in the `mentions.json` file, as specified in `application.conf`.

