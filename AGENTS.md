# AGENTS.md

Cliente Java del Europeana Search API v2 (`eu.europeana.api.client`). Git root ≠ módulo Maven.

## Layout

- Raíz del repo: `README.md`, `LICENSE.md`, este fichero.
- Módulo Maven: `europeana-client/` (`groupId` `baratz.es`, `0.3.0-SNAPSHOT`).
- Ejecutar **todos** los comandos Maven desde `europeana-client/`.

## Build y toolchain

- Java **8** (`java-release.version=8`); animal-sniffer (`java18`) en fase `test`.
- Maven ≥ 3.6; compilador `groovy-eclipse-compiler` (Java + Groovy).
- Dependencias de producción: Gson, SLF4J, commons-io. **Apache HttpClient es solo test.**

```sh
cd europeana-client
mvn clean test          # unitarios (Surefire)
mvn clean verify        # unitarios + IT (Failsafe)
mvn -Dtest=Api2QuerySpec test
mvn -Dit.test=SimpleSearchIT verify
```

## HTTP (crítico)

La librería **no** trae cliente HTTP. Hay que inyectar un `HttpConnector` con `UrlContentGetter`, `UrlFormPoster` y `UrlContentWriter`. Sin él, `BaseApiConnection` lanza `IllegalStateException`.

En tests: `ApacheHttpConnectors.create()` (`src/test/java/.../connection/`, scope test).

```java
EuropeanaApi2Client client = new EuropeanaApi2Client(http);
// o client.setHttpConnection(http);
```

## Tests

| Tipo | Patrón | Ubicación | Runner |
|------|--------|-----------|--------|
| Unit | `*Spec` | `src/test/groovy` | Surefire (Spock) |
| IT | `*IT` | `src/test/java` | Failsafe (`verify`) |

ITs necesitan API key real (red + Europeana):

```sh
cp src/main/resources/europeana-client.properties.template \
   src/test/resources/europeana-client.properties
# Rellenar europeana.api.key
```

`europeana-client.properties` está en `.gitignore`. **Nunca** commitearlo. `ClientConfiguration` lo carga del classpath como `/europeana-client.properties`.

## Arquitectura útil

- Entrada principal: `EuropeanaApi2Client` (también `MyEuropeanaClient`, `ProviderDatasetsClient`).
- Queries: `Api2Query` / `Api2QueryBuilder` / `EuropeanaQuery*`; param API `wskey`.
- Modelos JSON (Gson) bajo `model/` y `model/search/`.
- Config: `ClientConfiguration` (singleton) lee URI, key y carpetas de datasets/imágenes.

## Convenciones

- `.editorconfig`: CRLF por defecto; LF en `*.{sh,yml,json}`; indent 4 en Java/Groovy.
- En la práctica: fuentes **Java** suelen usar **tabs**; Specs **Groovy** usan **spaces**. Igualar el fichero tocado.
- `*.properties` charset latin1 según EditorConfig.
