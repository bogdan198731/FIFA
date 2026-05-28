# syntax=docker/dockerfile:1.7
#
# Multi-stage build for the World Cup backend.
#
# 1. `build`   — Maven + JDK 17 image, produces the fat JAR
# 2. `runtime` — JRE-only Alpine image, ~100 MB, runs as a non-root user
#
# Render's Docker runtime injects $PORT at boot, so the entrypoint passes it
# through with -Dserver.port=$PORT instead of hardcoding 8080.

# ----------------------------------------------------------------------------
# Stage 1: build
# ----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Pull dependencies first in their own layer so source-only changes don't
# invalidate the maven cache.
COPY pom.xml ./
RUN mvn -B -e -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -e -DskipTests package \
 && mv target/$(ls target/ | grep -E '^worldcup-.*\.jar$' | head -1) target/app.jar

# ----------------------------------------------------------------------------
# Stage 2: runtime
# ----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# BusyBox wget is available in Alpine — used by HEALTHCHECK.
RUN addgroup -S spring && adduser -S spring -G spring \
 && chown -R spring:spring /app
USER spring:spring

COPY --from=build --chown=spring:spring /workspace/target/app.jar /app/app.jar

# Sensible JVM defaults for a container — let the JVM see cgroup memory
# limits and use the urandom-backed entropy source so startup isn't slow.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Render overrides PORT; this default makes local `docker run` work too.
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --quiet --spider "http://localhost:${PORT}/api/health" || exit 1

# Use `exec` so the JVM becomes PID 1 and receives SIGTERM cleanly on
# container shutdown.
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT} -jar /app/app.jar"]
