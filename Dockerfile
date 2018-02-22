FROM quay.io/ukhomeofficedigital/openjdk8:latest

ENV USER user-borders-workflow-engine
ENV GROUP group-borders-workflow-engine
ENV NAME borders-workflow-engine

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r -g ${GROUP} ${USER} -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

ADD . /app/

RUN ./gradlew clean build

USER ${USER}

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom" , "-Dspring.profiles.active=$ENVIRONMENT", "-jar","/app/dist/libs/borders-workflow-engine.jar"]