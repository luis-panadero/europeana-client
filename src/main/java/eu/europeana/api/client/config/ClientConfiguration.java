package eu.europeana.api.client.config;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.api.client.exception.TechnicalRuntimeException;

public class ClientConfiguration implements EuropeanaApiConfiguration,
        ThumbnailAccessConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    protected static final String EUROPEANA_CLIENT_PROPERTIES_FILE = "/europeana-client.properties";
    // API CONFIG KEYS
    public static final String PROP_EUROPEANA_API_KEY = "europeana.api.key";
    public static final String PROP_EUROPEANA_API_URI = "europeana.api.uri";
    public static final String PROP_EUROPEANA_SEARCH_URN = "europeana.search.urn";
    public static final String PROP_EUROPEANA_RECORD_URN = "europeana.record.urn";
    // Thumbnail Access KEYS
    public static final String PROP_BASE_FOLDER_KEY = "europeana.client.base.folder";
    public static final String PROP_DATASETS_FOLDER_KEY = "europeana.client.datasets.folder";
    public static final String PROP_BASE_IMAGE_FOLDER_KEY = "europeana.client.base.image.folder";
    public static final String PROP_IMAGE_MIN_SIZE = "europeana.client.image.min.size";

    public static final String DEFAULT_API_URI = "https://api.europeana.eu/api/v2/";
    public static final String DEFAULT_SEARCH_URN = "search.json";
    public static final String DEFAULT_RECORD_URN = "record";

    // static configs
    public static final String DATASET_FILE_EXTENSION = ".csv";
    public static final String DEFAULT_IMAGE_MIN_SIZE = "1";

    // local attributes
    private static Properties properties = null;
    private static ClientConfiguration singleton;

    /**
     * Hide the default constructor
     */
    protected ClientConfiguration() {
    }

    /**
     * Accessor method for the singleton
     *
     * @return
     */
    public static synchronized EuropeanaApiConfiguration getInstance() {
        if (singleton == null) {
            singleton = new ClientConfiguration();
            singleton.loadProperties();
        }
        return singleton;
    }

    /**
     * Lazy loading of configuration properties. Falls back to built-in defaults
     * when the properties file is missing from the classpath.
     */
    public synchronized void loadProperties() {
        try {
            properties = new Properties();
            applyBuiltInDefaults(properties);
            InputStream resourceAsStream = getClass().getResourceAsStream(
                    EUROPEANA_CLIENT_PROPERTIES_FILE);
            if (resourceAsStream != null) {
                getProperties().load(resourceAsStream);
            } else {
                log.warn("No properties file found in classpath ({}). Using built-in defaults.",
                        EUROPEANA_CLIENT_PROPERTIES_FILE);
            }
        } catch (Exception e) {
            throw new TechnicalRuntimeException(
                    "Cannot read configuration file: "
                            + EUROPEANA_CLIENT_PROPERTIES_FILE, e);
        }
    }

    private static void applyBuiltInDefaults(Properties props) {
        props.setProperty(PROP_EUROPEANA_API_URI, DEFAULT_API_URI);
        props.setProperty(PROP_EUROPEANA_SEARCH_URN, DEFAULT_SEARCH_URN);
        props.setProperty(PROP_EUROPEANA_RECORD_URN, DEFAULT_RECORD_URN);
        props.setProperty(PROP_EUROPEANA_API_KEY, "");
        props.setProperty(PROP_IMAGE_MIN_SIZE, DEFAULT_IMAGE_MIN_SIZE);
    }

    /**
     * provides access to the configuration properties. It is not recommended to
     * use the properties directly, but the
     *
     * @return
     */
    protected Properties getProperties() {
        return properties;
    }

    /**
     *
     * @return the name of the file storing the client configuration
     */
    String getConfigurationFile() {
        return EUROPEANA_CLIENT_PROPERTIES_FILE;
    }

    /**
     * This method provides access to the API key defined in the configuration
     * file
     *
     * @see PROP_EUROPEANA_API_KEY
     *
     * @return
     */
    @Override
    public String getApiKey() {
        return getProperties().getProperty(PROP_EUROPEANA_API_KEY);
    }

    /**
     * This method provides access to the search uri value defined in the
     * configuration file
     *
     * @see PROP_EUROPEANA_SEARCH_URN
     *
     * @return
     */
    @Override
    public String getSearchUri() {
        return (getEuropeanaUri() + getSearchUrn());
    }

    /**
     * This method provides access to the search urn value defined in the
     * configuration file
     *
     * @see PROP_EUROPEANA_SEARCH_URN
     */
    @Override
    public String getSearchUrn() {
        return getProperties().getProperty(PROP_EUROPEANA_SEARCH_URN, DEFAULT_SEARCH_URN);
    }

    /**
     * This method provides access to the record uri value defined in the
     * configuration file
     *
     * @see PROP_EUROPEANA_RECORD_URN
     *
     * @return
     */
    @Override
    public String getRecordUri() {
        return (getEuropeanaUri() + getRecordUrn());
    }

    @Override
    public String getRecordUrn() {
        return getProperties().getProperty(PROP_EUROPEANA_RECORD_URN, DEFAULT_RECORD_URN);
    }

    @Override
    public String getDatasetsFolder() {
        return (getProperties().getProperty(PROP_DATASETS_FOLDER_KEY));
    }

    @Override
    public String getImageFolder(String dataset) {

        String ret = getBaseImageFolder();
        final String separator = "/";
        if (!ret.endsWith(separator))
            ret += separator;

        ret += dataset;

        return ret;
    }

    @Override
    public String getBaseFolder() {
        return (getProperties().getProperty(PROP_BASE_FOLDER_KEY));
    }

    @Override
    public String getBaseImageFolder() {
        return (getProperties().getProperty(PROP_BASE_IMAGE_FOLDER_KEY));
    }

    @Override
    public File getDatasetFile(String dataset) {
        return new File(getDatasetsFolder(), dataset + DATASET_FILE_EXTENSION);
    }

    @Override
    public String getEuropeanaUri() {
        return getProperties().getProperty(PROP_EUROPEANA_API_URI, DEFAULT_API_URI);
    }

    @Override
    public int getImageMinSize() {
        return Integer.valueOf(getProperties().getProperty(PROP_IMAGE_MIN_SIZE, DEFAULT_IMAGE_MIN_SIZE));
    }

    @Override
    public File getImageFile(File dir, String id) {
        String fileName = id + ".jpg";
        File imageFile = new File(dir, fileName);
        return imageFile;
    }


    @Override
    public String getMetadataFolder() {
            String datasetFolder = getDatasetsFolder();
            return datasetFolder + "/metadata/";
    }

    @Override
    public String getJsonMetadataFile(String id, String metadataFolder, String representation) {
        return metadataFolder + representation + id + ".json";
    }
}
