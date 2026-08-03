package eu.europeana.api.client.connection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.api.client.config.ClientConfiguration;

/**
 * Base HTTP connection for Europeana API clients (Search API v2 and related services).
 */
public class BaseApiConnection {

	private String apiKey;
	private String baseServiceUri = "";
	private HttpConnector httpConnection = new HttpConnector();
	protected Logger logger = LoggerFactory.getLogger(getClass().getName());

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getServiceUri() {
		return baseServiceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.baseServiceUri = serviceUri;
	}

	/**
	 * Alias of {@link #getServiceUri()} kept for query URL builders.
	 */
	public String getEuropeanaUri() {
		return getServiceUri();
	}

	/**
	 * Alias of {@link #setServiceUri(String)} kept for query URL builders.
	 */
	public void setEuropeanaUri(String europeanaUri) {
		setServiceUri(europeanaUri);
	}

	public HttpConnector getHttpConnection() {
		return httpConnection;
	}

	public void setHttpConnection(HttpConnector httpConnection) {
		this.httpConnection = httpConnection;
	}

	/**
	 * Create a new connection to the Annotation Service (REST API).
	 * 
	 * @param baseServiceUri
	 * 
	 * @param apiKey
	 *            API Key required to access the API
	 * 
	 */
	public BaseApiConnection(String baseServiceUri, String apiKey) {
		this.apiKey = apiKey;
		this.baseServiceUri = baseServiceUri;
	}

	/**
	 * Create a new connection to the Annotation Service (REST API) using the default configuration in the properties files
	 */
	public BaseApiConnection() {
		this(
				ClientConfiguration.getInstance().getEuropeanaUri(),
				ClientConfiguration.getInstance().getApiKey());
	}

	protected String getJSONResult(String url) throws IOException {
		logger.trace("Call to Annotation API (GET): " + url);
		return getHttpConnection().getURLContent(url);
	}

	protected String getJSONResult(String url, String paramName, String jsonPost)
			throws IOException {
		logger.trace("Call to Annotation API (POST): " + url);
		
		return getHttpConnection().getURLContent(url, paramName, jsonPost);
	}
}
