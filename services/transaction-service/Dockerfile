FROM eclipse-temurin:26-jdk

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -DskipTests

COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8083

CMD ["java", "-jar", "target/transaction-service-0.0.1-SNAPSHOT.jar"]