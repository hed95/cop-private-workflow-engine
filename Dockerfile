FROM quay.io/digitialpatterns/jdk:latest

WORKDIR /app

RUN mkdir -p /app && \
    chown -R java:java /app

ADD . /app/

RUN ./gradlew clean build -x test

USER java

EXPOSE 8080

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/dist/libs/workflow-engine.jar

