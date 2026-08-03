/*
 * HttpConnector.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.api.client.connection;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A HttpConnector is a class encapsulating simple HTTP access.
 *
 * @author Andres Viedma Pelez
 * @author Sergiu Gordea
 */
public class HttpConnector {

    private static final int CONNECTION_RETRIES = 3;
    private static final int TIMEOUT_CONNECTION = 900000;//15 minutes
    private static final int STATUS_OK_START = 200;
    private static final int STATUS_OK_END = 299;
    private static final String ENCODING = "UTF-8";
    private CloseableHttpClient httpClient = null;

    private static final Logger log = LoggerFactory.getLogger(HttpConnector.class);
	
    public String getURLContent(String url) throws IOException {
        CloseableHttpClient client = this.getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
        HttpGet getRequest = new HttpGet(url);

        try (CloseableHttpResponse response = client.execute(getRequest)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return "";
            }
            return EntityUtils.toString(entity, ENCODING);
        }
    }

    public boolean writeURLContent(String url, OutputStream out) throws IOException {
        return writeURLContent(url, out, null);
    }

    public boolean writeURLContent(String url, OutputStream out, String requiredMime) throws IOException {
        CloseableHttpClient client = this.getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
        HttpGet getRequest = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(getRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();

            Header contentTypeHeader = response.getFirstHeader("Content-Type");
            String contentType = "";
            if (contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
            }

            if (statusCode >= STATUS_OK_START && statusCode <= STATUS_OK_END
                    && ((requiredMime == null) || ((contentType != null) && contentType.contains(requiredMime)))) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return false;
                }
                try (InputStream in = entity.getContent()) {
                    byte[] b = new byte[4 * 1024];
                    int read;
                    while ((read = in.read(b)) != -1) {
                        out.write(b, 0, read);
                    }
                }
                return true;
            } else {
                EntityUtils.consumeQuietly(response.getEntity());
                return false;
            }
        }
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
            public void write(int b)
                    throws IOException {
            }
        };

        boolean bOk = this.silentWriteURLContent(url, out, requiredMime);
        return bOk;
    }

    private CloseableHttpClient getHttpClient(int connectionRetry, int connectionTimeout) {
        if (this.httpClient == null) {
            RequestConfig.Builder configBuilder = RequestConfig.custom();

            boolean bTimeout = false;
            String connectTimeOut = System.getProperty("sun.net.client.defaultConnectTimeout");
            if ((connectTimeOut != null) && (connectTimeOut.length() > 0)) {
                configBuilder.setConnectTimeout(Integer.parseInt(connectTimeOut));
                bTimeout = true;
            }
            String readTimeOut = System.getProperty("sun.net.client.defaultReadTimeout");
            if ((readTimeOut != null) && (readTimeOut.length() > 0)) {
                configBuilder.setSocketTimeout(Integer.parseInt(readTimeOut));
                bTimeout = true;
            }
            if (!bTimeout) {
                configBuilder.setConnectTimeout(connectionTimeout);
                configBuilder.setSocketTimeout(connectionTimeout);
            }

            HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(connectionRetry, false);

            HttpClientBuilder builder = HttpClients.custom()
                    .setRetryHandler(retryHandler)
                    .setDefaultRequestConfig(configBuilder.build());

            String proxyHost = System.getProperty("http.proxyHost");
            if ((proxyHost != null) && (proxyHost.length() > 0)) {
                String proxyPortSrt = System.getProperty("http.proxyPort");
                if (proxyPortSrt == null) {
                    proxyPortSrt = "8080";
                }
                int proxyPort = Integer.parseInt(proxyPortSrt);
                builder.setProxy(new HttpHost(proxyHost, proxyPort));
            }

            this.httpClient = builder.build();
        }
        return this.httpClient;
    }
    
    public String getURLContent(String url, String jsonParamName, String jsonParamValue) throws IOException {
        CloseableHttpClient client = this.getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
        HttpPost post = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(jsonParamName, jsonParamValue));
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = client.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= STATUS_OK_START && statusCode <= STATUS_OK_END) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return "";
                }
                return EntityUtils.toString(entity, ENCODING);
            } else {
                EntityUtils.consumeQuietly(response.getEntity());
                return null;
            }
        }
    }
}
