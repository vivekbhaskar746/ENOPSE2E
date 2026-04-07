# ---- Stage 1: Build ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy Maven wrapper + pom first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Copy source and build
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN useradd -r -u 10001 -s /bin/false appuser
WORKDIR /app

COPY --from=build /workspace/target/customer-support-portal-*.jar app.jar

# Default to prod profile when running in Docker
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

EXPOSE 1995

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:1995/api/faqs || exit 1

USER appuser
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
