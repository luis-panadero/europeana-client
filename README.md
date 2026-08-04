# Europeana-Client

Java client for the [Europeana Search API v2](https://pro.europeana.eu/page/search).

Forked from [Europeana4J](https://github.com/baratz-es/europeana4j) (originally [europeana4j on Google Code](http://code.google.com/p/europeana4j/)), mavenized and refactored.

- **Maven module:** `europeana-client/` (`groupId` `baratz.es`, version `0.3.0-SNAPSHOT`)
- **Java:** 8
- **Production dependencies:** Gson, SLF4J, commons-io

> Run all Maven commands from the `europeana-client/` directory.

## Contents

1. [Configuration](#configuration)
2. [HTTP transport](#http-transport)
3. [Main client](#main-client)
4. [Search](#search)
5. [Object retrieval](#object-retrieval)
6. [Thumbnails](#thumbnails)
7. [Metadata by collection](#metadata-by-collection)
8. [Providers](#providers)
9. [MyEuropeana](#myeuropeana)
10. [Tests](#tests)
11. [License](#license)

---

## Configuration

The library loads `/europeana-client.properties` from the classpath via `ClientConfiguration` (singleton).

Template:

```sh
cp europeana-client/src/main/resources/europeana-client.properties.template \
   europeana-client/src/test/resources/europeana-client.properties
```

Relevant properties:

| Property | Description |
|----------|-------------|
| `europeana.api.uri` | API base URI (`https://api.europeana.eu/api/v2/`) |
| `europeana.search.urn` | Search path (`search.json`) |
| `europeana.record.urn` | Record path (`record`) |
| `europeana.api.key` | API key (`wskey`) |
| `europeana.client.datasets.folder` | Datasets folder |
| `europeana.client.base.image.folder` | Folder for downloaded images |

**Never** commit `europeana-client.properties` (it is in `.gitignore`). Request an API key from [Europeana Pro](https://pro.europeana.eu/page/get-api).

You can also pass URI and key via constructor:

```java
EuropeanaApi2Client client = new EuropeanaApi2Client(
    "https://api.europeana.eu/api/v2/",
    "YOUR_API_KEY",
    http
);
```

---

## HTTP transport

The library does **not** ship a default HTTP client. You must inject an
`HttpConnector` built from three functional interfaces:

- `UrlContentGetter` — GET → `String`
- `UrlFormPoster` — POST form → `String`
- `UrlContentWriter` — GET → stream (return `true` on success)

Without it, `BaseApiConnection` throws `IllegalStateException`.

```java
HttpConnector http = new HttpConnector(
    url -> /* GET → String */,
    (url, name, value) -> /* POST form → String */,
    (url, out, mime) -> /* GET → stream; return true on success */
);

EuropeanaApi2Client client = new EuropeanaApi2Client(http);
// or: client.setHttpConnection(http);
```

Tests use Apache HttpClient (test scope only):

```java
HttpConnector http = ApacheHttpConnectors.create();
EuropeanaApi2Client client = new EuropeanaApi2Client(http);
```

---

## Main client

`EuropeanaApi2Client` is the entry point for Search API v2:

| Method | Purpose |
|--------|---------|
| `searchApi2(query, limit, start)` | Search with `start`/`rows` pagination |
| `searchApi2(query, cursor, rows)` | Search with cursor pagination |
| `searchApi2(portalSearchUrl, limit, start)` | Reuse a Europeana portal search URL |
| `getObject(id)` | Full record by Europeana ID |
| `getEuropeanaRecordResponse(id[, profile])` | Raw record JSON |
| `getQueryBuilder()` | Access to `Api2QueryBuilder` |
| `parseApiResponse(json)` | Parse search JSON into `EuropeanaApi2Results` |

Search results are `EuropeanaApi2Results` (items as `EuropeanaApi2Item`, facets, `nextCursor`, totals).

---

## Search

### Simple search with `Api2Query`

```java
HttpConnector http = /* your HttpConnector */;
EuropeanaApi2Client client = new EuropeanaApi2Client(http);

Api2Query query = new Api2Query();
query.setCreator("picasso");
query.setType(EuropeanaComplexQuery.TYPE.IMAGE); // IMAGE, TEXT, VIDEO, SOUND, 3D
query.setNotProvider("Hispana");

// limit=-1 → API default page size; start=1
EuropeanaApi2Results results = client.searchApi2(query, -1, 1);

System.out.println("Total: " + results.getTotalResults());
System.out.println("This page: " + results.getItemsCount());

for (EuropeanaApi2Item item : results.getAllItems()) {
    System.out.println(item.getTitle());
    System.out.println(item.getObjectURL());
    System.out.println(item.getType());
    System.out.println(item.getDcCreator());
    System.out.println(item.getEdmPreview());
    System.out.println(item.getDataProvider());
    System.out.println(item.getId());
}
```

Common `Api2Query` / `EuropeanaQuery` fields:

| Setter | API field |
|--------|-----------|
| `setGeneralTerms` | `text` (all fields) |
| `setCreator` | `who` |
| `setTitle` | `title` |
| `setWhatTerms` | `what` |
| `setSubject` | `subject` |
| `setDate` | `date` |
| `setType` | `TYPE` |
| `setProvider` / `setNotProvider` | `PROVIDER` |
| `setDataProvider` / `setNotDataProvider` | `DATA_PROVIDER` |
| `setCountry` | `COUNTRY` |
| `setLanguage` | `LANGUAGE` |
| `setCollectionName` / constructor | `europeana_collectionName` |
| `setProfile` | `profile` (e.g. `rich`) |
| `setWholeSubQuery` | free-form subquery |
| `addSubQuery(SubQuery)` | additional fields |
| `addQueryRefinement` | `qf` parameter |

Field constants: `EuropeanaFields`. Operators: `EuropeanaOperators` (`AND`, `OR`, `NOT`, …).

### Collection and refinements (`qf`)

```java
Api2Query query = new Api2Query("\"2020706_Ag_EU_CARARE_NPU\"");
query.setWhatTerms("building");
query.addQueryRefinement("NOT gips");
query.addQueryRefinement("NOT capitel");

EuropeanaApi2Results results = client.searchApi2(query, 10, 1);
```

### With `Api2QueryBuilder`

```java
Api2QueryInterface query = client.getQueryBuilder().buildQuery(
    "my_collection",  // collectionName
    "war",            // generalTerms
    "photography",    // what
    "Anonimous",      // creator
    "IMAGE",          // objectType
    "The European Library", // provider
    "Wellcome Library",     // dataProvider
    new String[] { "YEAR:1914", "YEAR:1918" } // refinements (qf)
);

EuropeanaApi2Results results = client.searchApi2(query, 12, 1);
```

You can also build from a portal or API URL:

```java
// Europeana portal URL
Api2QueryInterface fromPortal = client.getQueryBuilder()
    .buildQuery("https://www.europeana.eu/en/search?query=picasso&start=1&rows=12");

// search.json base URL (without wskey / start / rows)
Api2QueryInterface fromApi = client.getQueryBuilder()
    .buildBaseQuery("https://api.europeana.eu/api/v2/search.json?query=picasso");
```

### From portal URL (shortcut)

```java
String portalUrl =
    "https://www.europeana.eu/en/search?query=DATA_PROVIDER%3A%22Wellcome+Library%22"
    + "+Great+War&start=13&rows=12";

EuropeanaApi2Results results = client.searchApi2(portalUrl, 4, 1);
```

### Complex queries (`EuropeanaComplexQuery`)

For nested operators and fields:

```java
EuropeanaOperand opA = new EuropeanaOperand("Shakespeare");
EuropeanaOperand opB = new EuropeanaOperand("William");
EuropeanaOperand opC = new EuropeanaOperand("poetry");
EuropeanaOperand combined = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB);
combined.addOperand(EuropeanaOperators.AND, opC);

EuropeanaSearchTerm term = new EuropeanaSearchTerm(EuropeanaFields.TITLE, combined);
// or simply:
term = new EuropeanaSearchTerm(EuropeanaFields.CREATOR, "eminescu");

EuropeanaComplexQuery query = new EuropeanaComplexQuery(term);
query.setType(EuropeanaComplexQuery.TYPE.TEXT);

EuropeanaApi2Results results = client.searchApi2(query, 20, 0);
```

### Cursor pagination

Useful for large result sets without deep offsets (`Api2Query` required):

```java
Api2Query query = new Api2Query();
query.setGeneralTerms("monet");

String cursor = "*"; // first cursor
int rows = 100;

do {
    EuropeanaApi2Results page = client.searchApi2(query, cursor, rows);
    for (EuropeanaApi2Item item : page.getAllItems()) {
        // process item
    }
    cursor = page.getNextCursor();
} while (cursor != null && !cursor.isEmpty());
```

### Rich profile and facets

```java
Api2Query query = new Api2Query("2020706_*");
query.setProfile("rich");

EuropeanaApi2Results results = client.searchApi2(query, 12, 1);

if (results.getFacets() != null) {
    for (Facet facet : results.getFacets()) {
        System.out.println(facet.getName());
        for (FacetField field : facet.getFields()) {
            System.out.println("  " + field.getLabel() + " = " + field.getCount());
        }
    }
}
```

---

## Object retrieval

After a search, or with a known Europeana ID:

```java
EuropeanaApi2Results results = client.searchApi2(query, 10, 0);

for (EuropeanaApi2Item item : results.getAllItems()) {
    EuropeanaObject full = client.getObject(item.getId());
    System.out.println(full.getAbout());
    System.out.println(full.toString());
}

// Raw record JSON (optionally with profile)
String json = client.getEuropeanaRecordResponse("/9200397/BibliographicResource_3000118433322");
String richJson = client.getEuropeanaRecordResponse(
    "/9200397/BibliographicResource_3000118433322", "rich");
```

`EuropeanaObject` models the EDM record (proxies, aggregations, web resources, etc.) under `model/search/`.

---

## Thumbnails

`ThumbnailsAccessor` downloads thumbnails for a query:

```java
EuropeanaApi2Client client = new EuropeanaApi2Client(http);
ThumbnailsAccessor ta = new ThumbnailsAccessor(client);

Api2Query query = new Api2Query();
query.setType("IMAGE");
query.setGeneralTerms("da vinci");

File folder = new File("/tmp/europeana/images");
List<String> copied = ta.copyThumbnails(query, folder, 2); // max 2
```

For large datasets (CSV of IDs/URLs), use `LargeThumbnailsetProcessing` + `ThumbnailDownloader`:

```java
LargeThumbnailsetProcessing processing = new LargeThumbnailsetProcessing(datasetCsv);
ThumbnailDownloader downloader = new ThumbnailDownloader(downloadFolder);
downloader.setHttpConnection(http);
processing.addObserver(downloader);
processing.processThumbnailset(0, -1, 1000); // offset, limit, batch
```

---

## Metadata by collection

`MetadataAccessor` walks results (with cursor) and can persist JSON per item or per block:

```java
Api2QueryInterface query = new Api2Query("2020706_*");
query.setProfile("rich");

MetadataAccessor ma = new MetadataAccessor(query, client);
ma.setHttpConnection(http);
ma.setStoreBlockwiseAsJson(true);

Map<String, String> contentMap = ma.getContentMap(
    CommonMetadata.EDM_FIELD_IGNORE,
    CommonMetadata.START_BEGINING,
    CommonMetadata.LIMIT_ALL,
    MetadataAccessor.ERROR_POLICY_CONTINUE
);
```

Error policies: `ERROR_POLICY_RETHROW`, `ERROR_POLICY_STOP`, `ERROR_POLICY_IGNORE`, `ERROR_POLICY_CONTINUE`.

---

## Providers

`ProviderDatasetsClient` / `ProviderDatasetsClientImpl` calls the Providers API:

```java
ProviderDatasetsClient providers = new ProviderDatasetsClientImpl(http);

List<Provider> all = providers.getProvidersList();
List<Provider> page = providers.getProvidersList(0, 10, "es"); // offset, pageSize, country
Provider one = providers.getProvider("providerId");
```

> **Note:** the `/api/v2/providers.json` endpoint may return 404; the related IT is `@Ignore`. Use only if the service is available in your environment.

---

## MyEuropeana

`MyEuropeanaClient` / `MyEuropeanaClientImpl` parses MyEuropeana tags responses (it does not perform the authenticated HTTP call itself):

```java
MyEuropeanaClient my = new MyEuropeanaClientImpl();
TagsApiResponse tags = my.parseTagsApiResponse(jsonString);
// or from InputStream:
tags = my.parseTagsApiResponse(inputStream);
```

> **Note:** MyEuropeana is not generally available; the IT is `@Ignore`.

---

## Tests

From `europeana-client/`:

```sh
# Unit tests (Spock, *Spec under src/test/groovy)
mvn clean test

# Unit + integration tests (*IT under src/test/java; need network + API key)
mvn clean verify

# Single test
mvn -Dtest=Api2QuerySpec test
mvn -Dit.test=SimpleSearchIT verify
```

ITs require `src/test/resources/europeana-client.properties` with a valid `europeana.api.key`.

### Loading in Eclipse

```sh
git clone <repo>
```

File → Import → Existing Maven Projects… (import the `europeana-client` module).

Make sure you never commit `europeana-client.properties`.

---

## License

Licensed under the EUPL V.1.1.

For full details, see [LICENSE.md](LICENSE.md).
