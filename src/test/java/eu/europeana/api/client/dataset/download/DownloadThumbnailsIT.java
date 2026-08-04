package eu.europeana.api.client.dataset.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import eu.europeana.api.client.connection.ApacheHttpConnectors;
import eu.europeana.api.client.dataset.EuClientDatasetUtil;
import eu.europeana.api.client.thumbnails.download.ThumbnailDownloader;
import eu.europeana.api.client.thumbnails.processing.LargeThumbnailsetProcessing;

public class DownloadThumbnailsIT extends
        EuClientDatasetUtil {

    // public static String CLASS_WW1 = "ww1";
    
    //support running the test as stand alone class
    public static void main(String[] args) throws Exception {                    
        parseParams(args);      
        JUnitCore.main(DownloadThumbnailsIT.class.getCanonicalName());            
    }
    
    
    @Test
    public void downloadThumbnails() throws FileNotFoundException, IOException {

        ensureParamsInit();
        
        File datasetFile = getDatasetFile();
        File downloadFolder = getDatasetImageFolder();

        LargeThumbnailsetProcessing datasetDownloader = new LargeThumbnailsetProcessing(datasetFile);
        ThumbnailDownloader downloader = new ThumbnailDownloader(downloadFolder);
        downloader.setHttpConnection(ApacheHttpConnectors.create());
        datasetDownloader.addObserver(downloader);
        datasetDownloader.processThumbnailset(0, -1, 1000);
//      datasetDownloader.processThumbnailset(0, 21, 10);

        log.debug("Failes items count: " + datasetDownloader.getFailureCount());
        
    }


    protected void ensureParamsInit() {
        //if not sent through parameters set it to test.
        if(getDataset() == null)
            setDataset("test");
    }


    protected File getDatasetImageFolder() {
        File folder = new File(System.getProperty("java.io.tmpdir"),
                "europeana-client" + File.separator + getDataset() + File.separator + "images");
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException(
                    "Cannot create download folder: " + folder.getAbsolutePath());
        }
        return folder;
    }


    protected File getDatasetFile() {
        String resourcePath = "/europeanaclient/datasets/" + getDataset() + ".csv";
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Classpath resource not found: " + resourcePath);
        }
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid classpath resource URI: " + resourcePath, e);
        }
    }
    
    
}
