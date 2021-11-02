# docker

## build

To build the image, do

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
docker build -f ./docker/Dockerfile -t habitus:latest .
```

## run

To run the image with Java configured and default arguments of `-in /input -out /output -threads 4`, for compatability with xDD, use

```shell
docker run --env _JAVA_OPTIONS=-Xmx10g --volume `pwd`/in:/input --volume `pwd`/out:/output --user nobody habitus:latest
```

This is assuming then that there is a directory `./in` containing the input files and an `./out` for receiving the output files.  This is a typical configuration during development.  These are mapped in the command to `/input` and `/output` directories in the container.


For testing from the container's command line, it may be useful to insert these arguments as well: `-it --entrypoint /bin/bash`.

To replace the default arguments with your own, follow this template:

```shell
docker run --env _JAVA_OPTIONS=-Xmx10g --volume `pwd`/in:/input --volume `pwd`/out:/output --user nobody habitus:latest -in [inputDir] -out [outputDir] -threads [threadCount]
```

The `threadCount` should probably not be more than 6.
