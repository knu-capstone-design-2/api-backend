# 1단계: 빌드용 이미지 (Gradle + JDK 21)
FROM gradle:8.5.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test

# 2단계: 실행용 이미지 (JRE 21)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/api-backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
