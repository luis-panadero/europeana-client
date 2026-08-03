# Europeana-Client

This project implements a java client for the Europeana Search API v2.

The project was forked from Europeana4J (http://code.google.com/p/europeana4j/) / (https://github.com/baratz-es/europeana4j) mavenized and refactored.

### HTTP transport

The library does **not** ship a default HTTP client. You must inject an
`HttpConnector` built from three functional interfaces (`UrlContentGetter`,
`UrlFormPoster`, `UrlContentWriter`):

```java
HttpConnector http = new HttpConnector(
    url -> /* GET → String */,
    (url, name, value) -> /* POST form → String */,
    (url, out, mime) -> /* GET → stream; return true on success */
);

EuropeanaApi2Client client = new EuropeanaApi2Client(http);
// or: client.setHttpConnection(http);
```

Tests use Apache HttpClient via `ApacheHttpConnectors.create()` (test scope only).

### How run tests

```sh
cp src/main/resources/europeana-client.properties.template src/test/resources/europeana-client.properties
```
Edit it and put a valid APIKEY.

Run `mvn clean verify`

### Loading in Eclipse workspace

git clone ....

File => Import => Existing Maven Projects ...


make sure you never commit the europeana-client.properties


## License

Licensed under the EUPL V.1.1.

For full details, see [LICENSE.md](LICENSE.md).
