# =====================================================
# Stage 1: Build the Vue frontend with Node/Vite
# =====================================================
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ .
RUN npm run build

# =====================================================
# Stage 2: Nginx image serving the built assets + proxy
# =====================================================
FROM nginx:1.27-alpine

COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html
