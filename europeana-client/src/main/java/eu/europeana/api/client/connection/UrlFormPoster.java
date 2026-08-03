package eu.europeana.api.client.connection;

import java.io.IOException;

/**
 * Functional abstraction for HTTP POST with a single form-urlencoded parameter.
 */
@FunctionalInterface
public interface UrlFormPoster {

	String post(String url, String paramName, String paramValue) throws IOException;
}
