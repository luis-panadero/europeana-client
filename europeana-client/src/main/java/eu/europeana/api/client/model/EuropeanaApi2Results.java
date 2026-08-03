/*
 * EuropeanaResults.java - europeana4j
 * (C) 2011 Digibis S.L.
 */
package eu.europeana.api.client.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import eu.europeana.api.client.model.search.EuropeanaApi2Item;
import eu.europeana.api.client.response.abstracts.AbstractListResponse;

/**
 * A EuropeanaApi2Results is an object encapsulating the results of a query to
 * Europeana Search API v2. It can be the result of multiple calls to the
 * Europeana API with the same query (using pagination of results).
 *
 * @author Andres Viedma Pelaez
 */
public class EuropeanaApi2Results extends AbstractListResponse<EuropeanaApi2Item> {

	private String nextCursor;
	
    public void setNextCursor(String nextCursor) {
		this.nextCursor = nextCursor;
	}

	public EuropeanaApi2Results() {
    	setItems(new ArrayList<EuropeanaApi2Item>());
    }

    /**
     * Adds an item to the results.
     * @param item 
     */
    public void addItem(EuropeanaApi2Item item) {
        this.getItems().add(item);
    }


    /**
     * Returns a list of EuropeanaItem objects with all the stored results
     * @return 
     */
    public List<EuropeanaApi2Item> getAllItems() {
        if (this.getItems() == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(this.getItems());
        }
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void toJSON(Writer out) throws IOException {
        Gson gson = new Gson();
        JsonWriter out2 = new JsonWriter(out);
        gson.toJson(this, EuropeanaApi2Results.class, out2);
        out2.flush();
    }

    
    public String getNextCursor() {
    	return this.nextCursor;
    }
}
