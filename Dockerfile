FROM gradle:6.3-jdk11 AS builder
COPY . /app
WORKDIR /app

RUN ./gradlew clean bootJar

FROM adoptopenjdk/openjdk11:slim

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y vim wget net-tools software-properties-common telnet && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs build/libs/
COPY --from=builder /app/build/resources build/resources/
COPY --from=builder /app/build/classes/kotlin/main build/classes/
COPY --from=builder /app/scripts/docker/ build/

EXPOSE 8081

ENTRYPOINT ["sh", "build/start.sh"]
