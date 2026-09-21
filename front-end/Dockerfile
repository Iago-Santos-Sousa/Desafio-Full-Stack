FROM node:22.23.1-alpine AS build
WORKDIR /app
ARG VITE_MAX_UPLOAD_BYTES=2147483648
ARG VITE_API_URL=http://localhost:8080
ENV VITE_MAX_UPLOAD_BYTES=$VITE_MAX_UPLOAD_BYTES
ENV VITE_API_URL=$VITE_API_URL
COPY package.json package-lock.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build
FROM nginx:1.29.2-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
