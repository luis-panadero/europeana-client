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

/**
 * Test-only factory that builds an {@link HttpConnector} backed by Apache HttpClient.
 */
public final class ApacheHttpConnectors {

    private static final int CONNECTION_RETRIES = 3;
    private static final int TIMEOUT_CONNECTION = 900000;
    private static final int STATUS_OK_START = 200;
    private static final int STATUS_OK_END = 299;
    private static final String ENCODING = "UTF-8";

    private ApacheHttpConnectors() {
    }

    public static HttpConnector create() {
        ApacheHttpTransport transport = new ApacheHttpTransport();
        return new HttpConnector(transport, transport, transport);
    }

    private static final class ApacheHttpTransport
            implements UrlContentGetter, UrlFormPoster, UrlContentWriter {

        private CloseableHttpClient httpClient;

        @Override
        public String get(String url) throws IOException {
            CloseableHttpClient client = getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
            HttpGet getRequest = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(getRequest)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return "";
                }
                return EntityUtils.toString(entity, ENCODING);
            }
        }

        @Override
        public String post(String url, String jsonParamName, String jsonParamValue)
                throws IOException {
            CloseableHttpClient client = getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
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
                }
                EntityUtils.consumeQuietly(response.getEntity());
                return null;
            }
        }

        @Override
        public boolean write(String url, OutputStream out, String requiredMime)
                throws IOException {
            CloseableHttpClient client = getHttpClient(CONNECTION_RETRIES, TIMEOUT_CONNECTION);
            HttpGet getRequest = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(getRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();

                Header contentTypeHeader = response.getFirstHeader("Content-Type");
                String contentType = "";
                if (contentTypeHeader != null) {
                    contentType = contentTypeHeader.getValue();
                }

                if (statusCode >= STATUS_OK_START && statusCode <= STATUS_OK_END
                        && ((requiredMime == null)
                                || ((contentType != null) && contentType.contains(requiredMime)))) {
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
                }
                EntityUtils.consumeQuietly(response.getEntity());
                return false;
            }
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

                HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(
                        connectionRetry, false);

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
    }
}
