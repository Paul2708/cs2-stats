FROM gradle:8.4-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle shadowJar

FROM eclipse-temurin:21-jre AS runner

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]