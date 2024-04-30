FROM amazoncorretto:17-alpine-full
EXPOSE 8080:8080
RUN mkdir /app

# FFMPEG install
RUN apk update && \
    apk add ffmpeg

COPY ./build/libs/*-all.jar /app/ucic-docker.jar

ENTRYPOINT ["java","-jar","/app/ucic-docker.jar"]