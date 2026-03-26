# Stage 1: Compilazione dell'applicazione Angular
# Usa l'immagine ufficiale di Node.js basata su Alpine Linux (leggera)
FROM node:22-alpine AS builder
# Imposta la cartella di lavoro all'interno del container
WORKDIR /app
# Copia i file package.json e package-lock.json per l'installazione delle dipendenze
COPY package*.json ./
# Installa le dipendenze in modo pulito (basato sul package-lock.json)
RUN npm ci
# Copia tutto il resto del codice sorgente nel container
COPY . .
# Esegue la build di produzione dell'applicazione Angular
RUN npm run build

# Stage 2: Configurazione del server per servire i file (Nginx)
# Usa l'immagine ufficiale di Nginx basata su Alpine Linux
FROM nginx:alpine
# Copia i file compilati dallo strato precedente (builder) nella cartella corretta di Nginx
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
# Copia la configurazione personalizzata di Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Espone la porta 80, sulla quale Nginx è in ascolto
EXPOSE 80
