FROM openjdk:8

WORKDIR /app/

COPY ./target/universal/bin /app/bin
COPY ./target/universal/lib /app/lib

ENTRYPOINT ["/app/bin/variable-reader"]
CMD ["-in", "/input", "-out", "/output", "-threads", "4"]
