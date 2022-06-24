# docker

## build

The image can be built with a single `sbt` command or a series of shell commands.

### sbt

This option uses no files from this directory, so it is independent of instructions in `Dockerfile`.  Instead, it uses `docker.sbt` from the main project directory.

* To perform tests before containerization, execute `sbt dockerize`.
* If you are in a hurry and don't need to (re)test, use `sbt docker:publishLocal`.

This should result in images `habitus:[version]` and `habitus:latest`.  The version number is specified in `docker.sbt`.


### shell

To build the image, execute these commands:

```shell
sbt dist
cd target/universal
unzip habitus*.zip
cd habitus*/bin
rm main main.bat
rm variable-shell variable-shell.bat
rm variable-reader.bat
cd ../..
mv habitus*/bin habitus*/lib .
cd ../..
docker build -f ./docker/Dockerfile -t habitus:[version] .
docker image tag habitus:[version] habitus:latest
```

## run

To run the image with Java configured and default arguments of `-in /input -out /output -threads 4`, for compatability with xDD, use

```shell
docker run --env _JAVA_OPTIONS=-Xmx10g --volume `pwd`/in:/input --volume `pwd`/out:/output --user nobody habitus:latest
```

This is assuming then that there is a directory `./in` on the docker host containing the input files and an `./out` for receiving the output files.  This is a typical configuration during development.  The input directory should be readable by `nobody` and the output directory should be writable by `nobody`.  These are mapped by the command to `/input` and `/output` directories in the container.


For testing from the container's command line, it may be useful to insert these arguments as well: `-it --entrypoint /bin/bash`.

To replace the default arguments with your own, follow this template:

```shell
docker run --env _JAVA_OPTIONS=[javaMemorySpec] --volume [hostInputDir]:[containerInputDir] --volume [hostOutputDir]:[containerOutputDir] --user [user] habitus:latest -in [containerInputDir] -out [containerOutputDir] -threads [threadCount]
```

The `threadCount` should probably max out at 4.  More doesn't help much.

## publish

Add "clulab" to the tag and push to dockerhub.

```shell
docker image tag habitus:[version] clulab/habitus:[version]
docker image tag habitus:latest clulab/habitus:latest
docker push clulab/habitus:[version]
docker push clulab/habitus:latest
```
