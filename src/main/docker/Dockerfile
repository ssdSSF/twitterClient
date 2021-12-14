FROM docker.io/adoptopenjdk/openjdk16:latest

RUN mkdir /app
WORKDIR /app
COPY libs /app/libs
COPY ${project.artifactId}.jar /app
CMD ["java", "-jar", "/app/${project.artifactId}.jar"]
