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

# JVM tuned for a small container (512 MB on Render free tier). The defaults
# happily eat the whole instance on startup and the OOM-killer fires before
# Spring Boot finishes booting. The flags below bound each non-heap consumer:
#
#   MaxRAMPercentage=60          heap = ~60% of container, leaves room for
#                                metaspace, code cache, threads, and direct
#                                buffers (~150-200 MB combined on Boot 3)
#   MaxMetaspaceSize=160m        prevent unbounded metaspace growth
#   ReservedCodeCacheSize=64m    JIT code cache cap
#   UseSerialGC                  lowest-overhead GC for small heaps; G1 wants
#                                ~32 MB just for its own bookkeeping
#   Xss256k                      smaller per-thread stacks (default 1 MB ×
#                                200 Tomcat threads = up to 200 MB)
#   egd=urandom                  avoid slow blocking entropy on first start
#
# If you upgrade Render to Starter (2 GB) or larger, drop everything below
# down to just `MaxRAMPercentage=75.0` — these flags trade peak throughput
# for a smaller footprint.
ENV JAVA_TOOL_OPTIONS="\
-XX:MaxRAMPercentage=60.0 \
-XX:MaxMetaspaceSize=160m \
-XX:ReservedCodeCacheSize=64m \
-XX:+UseSerialGC \
-Xss256k \
-Djava.security.egd=file:/dev/./urandom"

# Cap Tomcat thread pool so it doesn't try to allocate 200 stacks on startup
# even when load is light. 50 is plenty for a tournament-sized app.
ENV SERVER_TOMCAT_THREADS_MAX=50
ENV SERVER_TOMCAT_THREADS_MIN_SPARE=5

# Render overrides PORT; this default makes local `docker run` work too.
ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --quiet --spider "http://localhost:${PORT}/api/health" || exit 1

# Use `exec` so the JVM becomes PID 1 and receives SIGTERM cleanly on
# container shutdown.
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT} -jar /app/app.jar"]
