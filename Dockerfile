FROM openjdk:21-jdk
LABEL authors="Vladislav"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENV POSTGRES_URL=jdbc:postgresql://localhost:5432/postgres
ENV POSTGRES_USER=postgres
ENV POSTGRES_PASSWORD=postgres
ENTRYPOINT ["java", "-jar", "app.jar"]