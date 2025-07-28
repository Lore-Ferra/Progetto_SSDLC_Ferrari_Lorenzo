# Progetto SSDLC - Secure Software Development Life Cycle

Lorenzo Ferrari
Repository: [Progetto SSDLC](https://github.com/Lore-Ferra/Progetto_SSDLC_Ferrari_Lorenzo)  
Progetto base: [onlinebookstore (Java)](https://github.com/shashirajraja/onlinebookstore)


## Obiettivo

Implementare un processo di sviluppo sicuro per un'applicazione Java, integrando pratiche DevSecOps tramite una pipeline CI/CD containerizzata.  
L’obiettivo è includere controlli di qualità, sicurezza e automazione delle build con strumenti open-source.


## Tecnologie Utilizzate

- Jenkins (automazione CI/CD)
- Docker & Docker Compose (ambiente containerizzato)
- SonarQube (analisi statica del codice)
- OWASP Dependency Check (analisi librerie di terze parti)
- PostgreSQL (per SonarQube)
- Maven (build Java)
- Jacoco (code coverage)


## Servizi disponibili

- Jenkins: http://localhost:8080  
- SonarQube: http://localhost:9001


## Pipeline CI/CD

La pipeline automatizza le seguenti fasi (in evoluzione):

1. Clonazione del repository
2. Build e test del progetto con Maven
3. Analisi statica del codice con SonarQube
4. Analisi delle dipendenze con OWASP Dependency Check
5. Controllo dei Quality Gate
6. Archiviazione degli artefatti (es. file `.jar`)

