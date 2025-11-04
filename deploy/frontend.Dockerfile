# build
FROM node:20-alpine AS build
WORKDIR /app
COPY Frontend/package*.json ./
RUN npm ci --no-audit --no-fund
COPY Frontend/ .
RUN npm run build

# run: vite preview 로 dist 서빙
FROM node:20-alpine
WORKDIR /app
# preview 실행 위해 devDeps도 필요 → node_modules 그대로
COPY --from=build /app /app
EXPOSE 3000
# host 바인딩
CMD ["npm","run","preview","--","--host","0.0.0.0","--port","3000","--strictPort"]