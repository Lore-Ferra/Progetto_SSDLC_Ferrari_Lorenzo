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

## Modifiche alle dipendenze per motivi di sicurezza

Durante la scansione delle librerie di terze parti tramite OWASP Dependency Check, sono state rilevate vulnerabilità critiche o alte in alcune dipendenze. Di seguito sono elencate le modifiche apportate rispetto alla versione originale del progetto.

| Libreria                   | Versione originale | Nuova versione | Vulnerabilità rilevate     | Azione           | Motivazione della modifica |
|----------------------------|--------------------|----------------|----------------------------|------------------|-----------------------------|
| `mysql-connector-java`     | 8.0.28             | 9.3.0          | CVE-2021-2471              | Aggiornata       | La versione 8.0.28 presenta almeno 2 vulnerabilità note, tra cui una che consente accesso non autorizzato ai metadati del database. |
| `postgresql`               | 42.3.7             | 42.7.7         | CVE-2022-21724             | Aggiornata       | La versione 42.3.7 era affetta da una vulnerabilità che poteva portare a denial of service o crash in specifiche condizioni di parsing. |
| `javax.servlet-api`        | 3.1.0              | 4.0.1          | CVE-2020-11996             | Aggiornata       | La versione 3.1.0 è affetta da una vulnerabilità DoS tramite richieste asincrone non gestite correttamente. |
| `junit-jupiter`            | Non presente       | 5.10.2         | -                          | Aggiunta         | Inserita per eseguire test moderni con supporto a JUnit 5. Nessuna CVE nota. |
| `mockito-core`             | Non presente       | 5.12.0         | -                          | Aggiunta         | Necessario per unit test e mocking. Versione aggiornata per evitare bug o falle nelle API di test. |
| `h2`                       | Non presente       | 2.2.224        | -                          | Aggiunta         | Usata per test database in memoria. Ultima versione stabile, priva di CVE rilevate. |
| `mockito-inline`           | Non presente       | 5.2.0          | -                          | Aggiunta         | Inserita per abilitare il mocking di metodi statici nel codice di test. Nessuna vulnerabilità nota. |

Tutte le modifiche sono state verificate tramite build Jenkins e analisi statica con SonarQube. Dopo ogni aggiornamento, è stato eseguito un nuovo ciclo di test e controllo dei Quality Gate.


