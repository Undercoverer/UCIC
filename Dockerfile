FROM openjdk:17.0.2
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*-all.jar /app/ucic-docker.jar
ENTRYPOINT ["java","-jar","/app/ucic-docker.jar"]