FROM eclipse-temurin:26-jdk

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -DskipTests

COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

CMD ["java", "-jar", "target/fraud-service-0.0.1-SNAPSHOT.jar"]