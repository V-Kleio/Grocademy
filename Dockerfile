FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

RUN chmod +x ./gradlew

RUN ./gradlew dependencies

COPY src/ src/

RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN mkdir -p /app/uploads/pdfs /app/uploads/videos

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV UPLOAD_DIR=/app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]