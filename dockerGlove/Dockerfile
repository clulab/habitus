# This should match the executables in bin.
FROM ubuntu:20.04

RUN apt-get update && \
    apt-get install --yes openjdk-8-jre-headless && \
    apt-get install --yes make && \
    apt-get install --yes gcc

WORKDIR /app/

COPY . /app
# User "nobody" must be able to write to this directory.
RUN chmod 777 /app
# User "nobody" must be able to execute this file.
RUN chmod 755 /app/glove.sh

RUN make

# turn this into a script that first runs
# this app, then starts the glove trainer
ENTRYPOINT ["./glove.sh"]
CMD ["/input", "/output"]
