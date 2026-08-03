# Europeana-Client

This project implements a java client for the Europeana Search Api.

The project was forked from Europeana4J (http://code.google.com/p/europeana4j/) / (https://github.com/baratz-es/europeana4j) mavenized and refactored.

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
