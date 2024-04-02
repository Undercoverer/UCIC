FROM amazoncorretto:17-alpine-full
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*-all.jar /app/ucic-docker.jar

# FFMPEG install
RUN apk update && \
    apk add ffmpeg

ENTRYPOINT ["java","-jar","/app/ucic-docker.jar"]