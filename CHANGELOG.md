# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Fork base: [`europeana/europeana-client`](https://github.com/europeana/europeana-client)
`0.2.2-SNAPSHOT` (`groupId` `eu.europeana.api`).

## [0.3.0-SNAPSHOT] - 2026-08-04

Fork maintained by Baratz (`groupId` `baratz.es`). Client focused on the
**Europeana Search API v2**, with injectable HTTP transport and a modernized
Maven toolchain targeting Java 8.

### Breaking changes

- **`groupId`**: `eu.europeana.api` → `baratz.es`; version `0.3.0-SNAPSHOT`.
- **HTTP is mandatory**: the library no longer ships a production HTTP client.
  Consumers must inject an `HttpConnector` built from `UrlContentGetter`,
  `UrlFormPoster`, and `UrlContentWriter`. Without it, `BaseApiConnection`
  throws `IllegalStateException`.
- **`httpclient` dependency**: moved to `test` scope. Production no longer
  pulls in Apache HttpClient; tests use `ApacheHttpConnectors.create()`.
- **Europeana API v1 removed**: `EuropeanaConnection` (OpenSearch) and the
  related v1 search flow are gone. Only API v2 (`EuropeanaApi2Client`) remains.
  See [europeana/europeana-client#37](https://github.com/europeana/europeana-client/issues/37).
- **Logging**: Log4J 1.x and `commons-logging` replaced by **SLF4J 1.7.36**
  (`slf4j-api` + `jcl-over-slf4j`). Logback is test-only.
- **Configuration**: missing `/europeana-client.properties` on the classpath no
  longer throws; built-in defaults are used (API v2 URI, `search.json`,
  `record`) and a warning is logged.

### Added

- Transport functional interfaces: `UrlContentGetter`, `UrlFormPoster`,
  `UrlContentWriter`; `HttpConnector` as an injectable façade.
- Test helper `ApacheHttpConnectors` (HttpComponents 4.5.14).
- Extra DC fields on `Proxy`: `dcTitle`, `dcLanguage`, `dcDate`,
  `dcPublisher`, `dcFormat`, `dcCoverage`, `dctermsIssued`.
- Built-in configuration defaults in `ClientConfiguration`
  (`DEFAULT_API_URI`, `DEFAULT_SEARCH_URN`, `DEFAULT_RECORD_URN`).
- Spock unit suite (`*Spec` under `src/test/groovy`): queries, operands,
  results, utilities, and object parsing.
- Surefire (unit) / Failsafe (IT `*IT`) split.
- Toolchain: Groovy 3 + `groovy-eclipse-compiler`, animal-sniffer (`java18`),
  JUnit 5 / Vintage, Spock 2.3.
- Operational docs: `AGENTS.md`, README expanded with API usage examples.
- `Jenkinsfile` for local CI (GitLab).
- `.editorconfig`, `.gitattributes`, and updated `.gitignore`
  (includes `europeana-client.properties`).

### Changed

- Production dependencies updated:
  - Gson `2.2.3` → `2.10.1`
  - commons-io `2.3` → `2.13.0`
  - commons-codec `1.7` → `1.16.0`
- Intermediate migration from `commons-httpclient` 3.1 to HttpComponents 4.5.14
  (production HTTP was later abstracted; HttpClient remains test-only).
- Repository layout: Maven module lives at the **repo root**
  (previously under `europeana-client/`).
- Java indentation: tabs → 4 spaces.
- IT URIs / fixtures aligned with the current Europeana API; thumbnail ITs use
  the system temp directory.
- `wholeSubQuery` construction in `EuropeanaQuery`: Lucene value is
  encoded when building `query=`.
- Record URL and configuration defaults revised for the current API v2.

### Fixed

- Infinite loop in search pagination when the API returned empty pages
  (fixed on the v1 client; that class was later removed with API v1).
- Incorrect escaping of `wholeSubQuery` clauses in search terms.
- Null-safe `toString` on models used by object ITs.

### Removed

- Full Europeana API v1 / OpenSearch support (`EuropeanaConnection` and related
  query/result APIs), per
  [europeana/europeana-client#37](https://github.com/europeana/europeana-client/issues/37).
- Production dependencies: `commons-httpclient`, `commons-logging`, Log4J 1.x.
- `log4j.xml.template` (replaced by `logback.xml` in tests).

### Dependencies (production)

| Artifact    | 0.2.2-SNAPSHOT     | 0.3.0-SNAPSHOT         |
|-------------|--------------------|------------------------|
| groupId     | `eu.europeana.api` | `baratz.es`            |
| HTTP        | commons-httpclient | *(injectable; no dep)* |
| Gson        | 2.2.3              | 2.10.1                 |
| Logging     | Log4J + JCL        | SLF4J 1.7.36           |
| commons-io  | 2.3                | 2.13.0                 |
| Java target | *(implicit)*       | 8 (animal-sniffer)     |

### Migration from 0.2.2-SNAPSHOT

1. Switch the Maven coordinate to `baratz.es:europeana-client:0.3.0-SNAPSHOT`.
2. Provide an `HttpConnector` when constructing the client:

   ```java
   EuropeanaApi2Client client = new EuropeanaApi2Client(http);
   // or client.setHttpConnection(http);
   ```

3. Replace any use of `EuropeanaConnection` / API v1 with
   `EuropeanaApi2Client`.
4. Ensure an SLF4J binding in the application (the library only exposes the API).
5. Keep the API key and v2 URI on the classpath or pass them via constructor;
   the properties file is no longer required to start.

[0.3.0-SNAPSHOT]: https://git.digibis.com/digibis1/librerias/europeana-client
