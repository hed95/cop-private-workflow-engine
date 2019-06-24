FROM digitalpatterns/jdk:2

ADD . /app/

WORKDIR /app

USER root

RUN ./gradlew clean build -x test && \
    mkdir /etc/keystore && \
    chown 1000 /etc/keystore

EXPOSE 8080

USER 1000
