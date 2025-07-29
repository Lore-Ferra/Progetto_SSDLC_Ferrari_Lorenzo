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

| Libreria               | Versione originale | Nuova versione | Vulnerabilità rilevate | Azione     | Motivazione della modifica                                                                                                              |
| ---------------------- | ------------------ | -------------- | ---------------------- | ---------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `mysql-connector-java` | 8.0.28             | 9.3.0          | CVE-2021-2471          | Aggiornata | La versione 8.0.28 presenta almeno 2 vulnerabilità note, tra cui una che consente accesso non autorizzato ai metadati del database.     |
| `postgresql`           | 42.3.7             | 42.7.7         | CVE-2022-21724         | Aggiornata | La versione 42.3.7 era affetta da una vulnerabilità che poteva portare a denial of service o crash in specifiche condizioni di parsing. |
| `javax.servlet-api`    | 3.1.0              | 4.0.1          | CVE-2020-11996         | Aggiornata | La versione 3.1.0 è affetta da una vulnerabilità DoS tramite richieste asincrone non gestite correttamente.                             |
| `junit-jupiter`        | Non presente       | 5.10.2         | -                      | Aggiunta   | Inserita per eseguire test moderni con supporto a JUnit 5. Nessuna CVE nota.                                                            |
| `mockito-core`         | Non presente       | 5.12.0         | -                      | Aggiunta   | Necessario per unit test e mocking. Versione aggiornata per evitare bug o falle nelle API di test.                                      |
| `h2`                   | Non presente       | 2.2.224        | -                      | Aggiunta   | Usata per test database in memoria. Ultima versione stabile, priva di CVE rilevate.                                                     |
| `mockito-inline`       | Non presente       | 5.2.0          | -                      | Aggiunta   | Inserita per abilitare il mocking di metodi statici nel codice di test. Nessuna vulnerabilità nota.                                     |

Tutte le modifiche sono state verificate tramite build Jenkins e analisi statica con SonarQube. Dopo ogni aggiornamento, è stato eseguito un nuovo ciclo di test e controllo dei Quality Gate.

## Risoluzione delle Vulnerabilità

### Vulnerabilità 1 - (Low)

Nella classe StoreException, i campi errorCode, errorMessage e statusCode sono stati resi final per impedirne la modifica dopo l’inizializzazione.

**Prima:**

```java
private String errorCode;
private String errorMessage;
private int statusCode;
```

**Dopo:**

```java
private final String errorCode;
private final String errorMessage;
private final int statusCode;
```

### Motivazione

Garantire l’immutabilità dei campi legati agli errori evita modifiche indesiderate durante il ciclo di vita dell’oggetto.  
In contesti multi-threaded o non controllati, i setter avrebbero potuto essere sfruttati per alterare informazioni critiche come il messaggio o il codice di errore.

### Vulnerabilità Mitigata

- **Tipo:** Modificabilità dello stato interno di oggetti di errore
- **Descrizione:** La presenza di metodi setter espone la classe a modifiche runtime non desiderate, con il rischio di:
  - generare log incoerenti
  - falsificare i messaggi di errore
  - introdurre comportamenti imprevedibili

### Classificazione OWASP

- **Categoria:** **A05 – Security Misconfiguration** (OWASP Top 10)
- **Gravità:** Bassa (Low)
- **Rischio:** Esporre strutture critiche a configurazioni deboli o modificabili può portare a comportamenti imprevisti e vulnerabilità sfruttabili.

### Benefici della Correzione

- Migliore **integrità** e **affidabilità** dell’oggetto StoreException
- Prevenzione di **manipolazioni a runtime**
- Codice più \*sicuro, **chiaro** e conforme alle **best practice DevSecOps**
- Maggiore **tracciabilità** e **coerenza nei log di errore**

## Vulnerabilità 2 – (Low)

In alcune parti del codice, veniva utilizzata una dichiarazione esplicita del tipo generico nel costruttore di `ArrayList`.

**Prima:**

```java
List<Book> books = new ArrayList<Book>();
```

**Dopo:**

```java
List<Book> books = new ArrayList<>();
```

### Motivazione

L’utilizzo del diamond operator (`<>`) introdotto in Java 7 evita la duplicazione ridondante del tipo e migliora:

- La **leggibilità** del codice
- La **manutenibilità**
- La **sicurezza evolutiva** del codice (se cambia il tipo, si modifica una sola volta)
- La **conformità** con le best practice di sviluppo moderne

### Classificazione OWASP

- **Categoria:** Non direttamente classificabile in OWASP Top 10, ma associabile a:
  - **A06 – Vulnerable and Outdated Components** (utilizzo di pattern non aggiornati)
- **Gravità:** Bassa (Low)
- **Rischio:** Nessun rischio diretto di sicurezza, ma può contribuire a una base di codice obsoleta e fragile, più esposta a errori futuri.

### Benefici della correzione

- Migliore **conformità** agli standard Java
- Codice più **chiaro** e privo di duplicazioni inutili
- Facilita il lavoro in team e l’integrazione con tool statici come **SonarQube**
- Riduce la possibilità di **refusi** durante il refactoring

### Vulnerabilità 3 – (Medium)

In alcune servlet (es. `CartServlet`) veniva dichiarato un campo **di istanza mutabile** per una dipendenza condivisa (`BookService`), violando le best practice per la gestione dei componenti in ambienti multithread come le servlet.

**Prima:**

```java
BookService bookService = new BookServiceImpl();
```

**Dopo:**

```java
private static final BookService bookService = new BookServiceImpl();
```

#### Motivazione

Le servlet sono **singleton gestiti dal container**: un solo oggetto `CartServlet` serve molteplici richieste concorrenti. Usare campi **non statici e non finali** può:

- Indurre in errore (sembrano specifici per richiesta, ma non lo sono)
- Essere fonte di bug se accidentalmente mutati
- Creare problemi di thread safety in caso di modifiche future

La modifica a `static final` garantisce che la dipendenza:

- sia **immutabile**
- sia **unica e condivisa**
- rispetto le **best practice**

#### Classificazione OWASP

- **Categoria:**
  - A05 – Security Misconfiguration
  - A04 – Insecure Design (per cattiva progettazione dello scope degli oggetti)
- **Gravità:** Media (Medium)
- **Rischio:** Medio. Anche se non introduce direttamente una vulnerabilità exploitabile, può causare condizioni di concorrenza o instabilità se il campo viene mutato da più thread.

#### Benefici della correzione

- Conformità ai principi di progettazione sicura per servlet
- Miglioramento della **thread safety**
- Rende il codice più **leggibile** e **testabile**
- Evita **ambiguità semantica** sullo scope del campo
- Agevola strumenti di analisi statica come **SonarQube**

### Vulnerabilità 4 – (Medium)

In alcune servlet (es. `CustomerRegisterServlet`, `CartServlet`) mancava l'annotazione `@Override` sopra la dichiarazione del metodo `service(...)`.

**Prima:**

```java
public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
```

**Dopo:**

```java
@Override
public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
```

#### Motivazione

L'annotazione `@Override` è essenziale per garantire che un metodo stia effettivamente sovrascrivendo una definizione nella superclasse (`HttpServlet`). La sua assenza può causare:

- **Errori silenziosi**, ad esempio quando si sbaglia la firma del metodo (nome, parametri o tipo di ritorno)
- **Comportamenti inattesi** in fase di esecuzione, perché il metodo definito non viene mai invocato
- **Difficoltà** nell'uso di strumenti di analisi statica o refactoring automatico

#### Classificazione OWASP

- **Categoria:**
  - A06 – Vulnerable and Outdated Components (cattivo uso delle funzionalità del linguaggio)
- **Gravità:** Media (Medium)
- **Rischio:** Potenziale malfunzionamento dell’applicazione in caso di firma errata non rilevata a compilazione. Non è una falla di sicurezza diretta, ma può causare instabilità o bug difficili da diagnosticare.

#### Benefici della correzione

- Evita **errori di overriding silenziosi**
- Migliora la **leggibilità** e la **manutenibilità** del codice
- Favorisce l'integrazione con strumenti come **SonarQube**, **IDE**, **CI/CD**
- Rende il comportamento della servlet **più prevedibile e corretto**

## Vulnerabilità 5 – (Medium)

In alcune servlet, come `ErrorHandlerServlet`, veniva utilizzato `System.out.println(...)` per la stampa di messaggi diagnostici e di errore.

**Prima:**

```java
System.out.println("======ERROR TRIGGERED========");
System.out.println("Servlet Name: " + servletName);
System.out.println("Request URI: " + requestUri);
System.out.println("Status Code: " + statusCode);
System.out.println("Error Code: " + errorCode);
System.out.println("Error Message: " + errorMessage);
System.out.println("=============================");
```

**Dopo:**

```java
logger.info("======ERROR TRIGGERED========");
logger.info(String.format("Servlet Name: %s", servletName));
logger.info(String.format("Request URI: %s", requestUri));
logger.info(String.format("Status Code: %d", statusCode));
logger.info(String.format("Error Code: %s", errorCode));
logger.info(String.format("Error Message: %s", errorMessage));
logger.info("=============================");
```

### Motivazione

L’utilizzo diretto di `System.out` in ambienti enterprise o concorrenti è considerato una cattiva pratica perché:

- Non è configurabile (non si può disattivare, filtrare o reindirizzare facilmente)
- Non fornisce livelli di severità (INFO, WARNING, SEVERE...)
- Non è thread-safe
- Compromette la leggibilità dei log in produzione
- Rende difficile l’integrazione con strumenti di monitoraggio e log analysis (es. ELK, Splunk)

L’uso del logger standard (`java.util.logging.Logger`) permette invece di:

- Distinguere tra log informativi, di errore o di debug
- Centralizzare la configurazione dei log
- Rendere il codice più pulito e professionale

### Classificazione OWASP

- **Categoria:**

  - A06 – Vulnerable and Outdated Components
  - A05 – Security Misconfiguration (per ambienti non correttamente configurati alla gestione dei log)

- **Gravità:** Media (Medium)
- **Rischio:** Moderato. Non espone direttamente a un attacco, ma può:
  - Nascondere o perdere informazioni importanti in produzione
  - Rendere più difficile il troubleshooting
  - Rendere il sistema non conforme agli standard aziendali (es. PCI-DSS, ISO 27001)

### Benefici della correzione

- Maggiore visibilità degli errori reali in ambienti multiutente
- Integrazione nativa con sistemi di log e APM
- Codice più professionale e pronto per la produzione
- Separazione delle responsabilità tra log e output standard
