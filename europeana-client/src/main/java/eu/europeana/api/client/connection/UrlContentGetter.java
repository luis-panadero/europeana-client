package eu.europeana.api.client.connection;

import java.io.IOException;

/**
 * Functional abstraction for HTTP GET returning the response body as a String.
 */
@FunctionalInterface
public interface UrlContentGetter {

    String get(String url) throws IOException;
}
