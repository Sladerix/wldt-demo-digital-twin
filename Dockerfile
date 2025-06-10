#FROM openjdk:24-jdk-slim
#WORKDIR /app
#COPY target/WLDT-Demo-DigitalTwin-*-SNAPSHOT-jar-with-dependencies.jar /app/app.jar
#EXPOSE 19090
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven AS maven
COPY ./pom.xml ./pom.xml
COPY ./src ./src
RUN mvn dependency:go-offline -B
RUN mvn clean install

FROM openjdk:24-jdk-slim
COPY target/WLDT-Demo-DigitalTwin-*-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 19090
ENTRYPOINT ["java", "-jar", "app.jar"]