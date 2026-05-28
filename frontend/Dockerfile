# syntax=docker/dockerfile:1.7
#
# Frontend image — Angular bundle served by nginx.
#
# The production deploy lives on Cloudflare Pages and doesn't need this file;
# it's here for local end-to-end testing (`docker compose up`) and for
# anywhere that prefers a static-site container over Cloudflare.

# ----------------------------------------------------------------------------
# Stage 1: build
# ----------------------------------------------------------------------------
FROM node:20-alpine AS build
WORKDIR /workspace

# Install deps in their own layer.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npx ng build --configuration=production

# ----------------------------------------------------------------------------
# Stage 2: runtime
# ----------------------------------------------------------------------------
FROM nginx:1.27-alpine AS runtime

# SPA fallback + cache headers — see nginx.conf for the rule set.
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --quiet --spider http://localhost/ || exit 1
