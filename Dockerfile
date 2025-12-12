# Multi-stage Dockerfile (Build-Run)
# 해당 파일은 각 도커 이미지의 Build-Run을 위한 설정 파일이라고 생각하면 된다.

#
# Build stage: compile the Spring Boot application into an executable jar
#
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy all the files into /app
COPY . .

# Build the bootable jar (skipping tests for faster image builds; adjust if needed)
RUN ./gradlew clean build -x test

#
# Runtime stage: lightweight JRE image
#
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the jar created in the build stage
COPY --from=builder /app/build/libs/*.jar backend.jar

# 컨테이너가 8080 port를 사용할 것임을 명시
EXPOSE 8080

# 컨테이너 시작 시 JAR 파일을 실행하는 명령어 설정
ENTRYPOINT ["java","-jar","backend.jar"]