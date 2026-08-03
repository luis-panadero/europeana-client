/*
 * HttpConnector.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.api.client.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A HttpConnector encapsulates simple HTTP access by delegating to injectable
 * functional transports. There is no default HTTP implementation; callers must
 * supply {@link UrlContentGetter}, {@link UrlFormPoster} and {@link UrlContentWriter}.
 *
 * @author Andres Viedma Pelez
 * @author Sergiu Gordea
 */
public class HttpConnector {

	private static final Logger log = LoggerFactory.getLogger(HttpConnector.class);

	private final UrlContentGetter contentGetter;
	private final UrlFormPoster formPoster;
	private final UrlContentWriter contentWriter;

	public HttpConnector(UrlContentGetter contentGetter, UrlFormPoster formPoster,
			UrlContentWriter contentWriter) {
		this.contentGetter = Objects.requireNonNull(contentGetter, "contentGetter");
		this.formPoster = Objects.requireNonNull(formPoster, "formPoster");
		this.contentWriter = Objects.requireNonNull(contentWriter, "contentWriter");
	}

	public String getURLContent(String url) throws IOException {
		return contentGetter.get(url);
	}

	public boolean writeURLContent(String url, OutputStream out) throws IOException {
		return writeURLContent(url, out, null);
	}

	public boolean writeURLContent(String url, OutputStream out, String requiredMime)
			throws IOException {
		return contentWriter.write(url, out, requiredMime);
	}

	public boolean silentWriteURLContent(String url, OutputStream out, String mimeType) {
		try {
			return this.writeURLContent(url, out, mimeType);
		} catch (Exception e) {
			log.debug("Exception occured when copying thumbnail from url: " + url, e);
			return false;
		}
	}

	public boolean checkURLContent(String url, String requiredMime) {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		};
		return this.silentWriteURLContent(url, out, requiredMime);
	}

	public String getURLContent(String url, String jsonParamName, String jsonParamValue)
			throws IOException {
		return formPoster.post(url, jsonParamName, jsonParamValue);
	}
}
