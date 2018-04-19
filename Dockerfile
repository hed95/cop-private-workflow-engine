FROM quay.io/ukhomeofficedigital/openjdk8:latest

WORKDIR /app

RUN groupadd -g 1000 -r engine && \
    useradd -r -g engine -u 1000 engine -d /app && \
    mkdir -p /app && \
    chown -R engine:engine /app

ADD . /app/

RUN ./gradlew clean build -x test

USER ${USER}

EXPOSE 8080

ENTRYPOINT ["java", " ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom" , "-jar","/app/dist/libs/borders-workflow-engine.jar"]
