FROM openjdk:8

WORKDIR /app/

COPY ./app/ /app

# turn this into a script that first runs
# this app, then starts the glove trainer
ENTRYPOINT ["./glove.sh"]
CMD ["/input", "/output"]
