FROM node:22-alpine AS build

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html vite.config.js ./
COPY public public
COPY src src
RUN npm run build

FROM nginx:alpine

RUN sed -i 's/listen       80;/listen       5173;/; s/listen  \[::\]:80;/listen [::]:5173;/; /location \/ {/i\    location /api/ {\
		proxy_pass http://gateway:8080;\
		proxy_http_version 1.1;\
		proxy_set_header Host $host;\
		proxy_set_header X-Real-IP $remote_addr;\
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\
		proxy_set_header X-Forwarded-Proto $scheme;\
	}' /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 5173
