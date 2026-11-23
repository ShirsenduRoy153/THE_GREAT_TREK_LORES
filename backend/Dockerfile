# ==============================================================================
# Multi-Stage Dockerfile for The Great Trek Lores (Spring Boot 3 / Java 17 Temurin)
# Optimized for 512MB RAM Free Tier (Render / Koyeb / Fly.io)
# ==============================================================================

# --- Stage 1: Build JAR ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package production JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime Environment ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user execution
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy compiled JAR
COPY --from=builder --chown=spring:spring /build/target/*.jar app.jar

# ------------------------------------------------------------------------------
# JVM Performance Tuning for 512MB RAM Free Tier:
# -XX:+UseSerialGC: Serial GC has minimal memory footprint (<15MB vs 120MB+ G1GC)
# -Xmx256m: Bounds max heap to 256MB, leaving 256MB for OS, metaspace, and thread stacks
# -Xms128m: Initial heap allocation
# -Xss512k: Slashes per-thread stack overhead by 50% (saving ~50MB across 30 threads)
# -XX:MaxMetaspaceSize=128m: Bounds native metaspace growth
# -Djava.security.egd: Fast non-blocking entropy for secure random seed generation
# ------------------------------------------------------------------------------
ENV JAVA_OPTS="-XX:+UseSerialGC -Xmx256m -Xms128m -Xss512k -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
