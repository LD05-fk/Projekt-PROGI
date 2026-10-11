# Web image za produkciju: izgrađeni frontend + Caddy (HTTPS, reverse proxy na backend).
# Gradi se iz korijena repozitorija: docker build -f deploy/web.Dockerfile .
FROM node:22 AS build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM caddy:2
COPY deploy/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /app/frontend/dist /srv
