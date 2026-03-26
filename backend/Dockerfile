# Stage 1: Compilazione dell'applicazione Spring Boot
# Usa l'immagine ufficiale di Maven con Java 25 (Temurin)
FROM maven:3.9-eclipse-temurin-25 AS builder
# Imposta la cartella di lavoro all'interno del container
WORKDIR /app
# Copia solo il file pom.xml per scaricare prima le dipendenze
COPY pom.xml .
# Scarica le dipendenze offline per ottimizzare i tempi di build sfruttando la cache dei layer Docker
RUN mvn dependency:go-offline -q
# Copia il codice sorgente nel container
COPY src ./src
# Compila l'applicazione creando un file JAR (saltando l'esecuzione dei test)
RUN mvn package -DskipTests -q

# Stage 2: Esecuzione dell'applicazione
# Usa un'immagine più leggera con solo il Java Runtime Environment (JRE)
FROM eclipse-temurin:25-jre
# Imposta la cartella di lavoro all'interno del nuovo container
WORKDIR /app
# Copia il file JAR precedentemente compilato dallo stage 'builder'
COPY --from=builder /app/target/*.jar app.jar
# Definisce un punto di mount per la directory degli upload
VOLUME /app/uploads
# Informa Docker che il container è in ascolto sulla porta 8080
EXPOSE 8080
# Comando da avviare quando parte il container limitando la memoria per evitare crash su cloud gratuiti
ENTRYPOINT ["java", "-Xmx256m", "-Xss512k", "-jar", "app.jar"]
