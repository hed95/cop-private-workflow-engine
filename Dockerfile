FROM digitalpatterns/jdk:4

ADD . /app/

WORKDIR /app

USER root

RUN ./gradlew clean build -x test

EXPOSE 8080

USER 1000
