# docker

## build

To build the image, execute these commands:

```shell
sbt dist
cd target/universal
unzip habitus*.zip
mv habitus*/lib ../../dockerGlove/app
mv habitus*/bin/train-glove-app ../../dockerGlove/app/bin
cd ../../dockerGlove
docker build -f ./Dockerfile -t habitus-glove:[version] ./app
docker image tag habitus-glove:[version] habitus-glove:latest
cd ..
```

## run

To run the image with Java configured and default arguments of `/input /output`, for compatability with xDD, use

```shell
docker run --env _JAVA_OPTIONS=-Xmx10g --volume `pwd`/in:/input --volume `pwd`/out:/output --user nobody habitus-glove:latest
```

This is assuming then that there is a directory `./in` on the docker host containing the input files and an `./out` for receiving the output files.  This is a typical configuration during development.  The input directory should be readable by `nobody` and the output directory should be writable by `nobody`.  These are mapped by the command to `/input` and `/output` directories in the container.


For testing from the container's command line, it may be useful to insert these arguments as well: `-it --entrypoint /bin/bash`.  From the container's command line, `./glove.sh` can be run manually.

To replace the default arguments with your own, follow this template:

```shell
docker run --env _JAVA_OPTIONS=[javaMemorySpec] --volume [hostInputDir]:[containerInputDir] --volume [hostOutputDir]:[containerOutputDir] --user [user] habitus:latest [containerInputDir] [containerOutputDir]
```

Several output files are produced.  The most important is `vectors.txt`, because it contains the trained vectors.  Also of interest is `vocab.txt` which contains word counts which can be spot checked for problems.  `glove.txt` contains tokenized text for all the documents, so it should be treated as private and not returned.



## publish

Add "clulab" to the tag and push to dockerhub.

```shell
docker image tag habitus-glove:[version] clulab/habitus-glove:[version]
docker image tag habitus-glove:latest clulab/habitus-glove:latest
docker push clulab/habitus-glove:[version]
docker push clulab/habitus-glove:latest
```
