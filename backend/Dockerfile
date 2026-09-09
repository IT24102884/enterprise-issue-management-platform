# ───── Stage 1: Build with Maven ─────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom first (layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ───── Stage 2: Runtime image ─────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user for security
RUN addgroup -S pmgroup && adduser -S pmuser -G pmgroup

COPY --from=builder /app/target/*.jar app.jar

RUN chown pmuser:pmgroup app.jar
USER pmuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
