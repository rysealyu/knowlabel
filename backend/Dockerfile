FROM alpine/java:21-jdk AS builder
WORKDIR /app

COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

