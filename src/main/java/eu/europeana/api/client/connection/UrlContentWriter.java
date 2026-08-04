package eu.europeana.api.client.connection;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Functional abstraction for HTTP GET that streams the response body to an
 * {@link OutputStream}, optionally validating the Content-Type.
 *
 * @return {@code true} if the content was written successfully; {@code false}
 *         otherwise (e.g. non-2xx status or MIME mismatch)
 */
@FunctionalInterface
public interface UrlContentWriter {

    boolean write(String url, OutputStream out, String requiredMime) throws IOException;
}
