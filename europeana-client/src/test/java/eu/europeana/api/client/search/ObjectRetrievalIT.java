package eu.europeana.api.client.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.connection.ApacheHttpConnectors;
import eu.europeana.api.client.exception.EuropeanaApiProblem;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.model.search.EuropeanaApi2Item;
import eu.europeana.api.client.model.search.EuropeanaObject;
import eu.europeana.api.client.search.query.Api2Query;

public class ObjectRetrievalIT {

	@Test
	public void test() throws IOException, EuropeanaApiProblem {
		EuropeanaApi2Client ec = new EuropeanaApi2Client(ApacheHttpConnectors.create());
		Api2Query query = new Api2Query();
		
		query.setCollectionName("2020706_*");
        EuropeanaApi2Results results = ec.searchApi2(query, 10, 0);
        assertTrue(results.getItemsCount() > 0);
        
        for(EuropeanaApi2Item result : results.getAllItems()) {
        	EuropeanaObject eo = ec.getObject(result.getId());
        	assertNotNull(eo);
        	assertNotNull(eo.getAbout());
        	System.out.println(eo.toString());
        }
	}	
	
	@Test
	public void testCompleteObject() throws IOException, EuropeanaApiProblem {
		EuropeanaApi2Client ec = new EuropeanaApi2Client(ApacheHttpConnectors.create());
		Api2Query query = new Api2Query();
		query.setGeneralTerms("europeana_completeness:10");
		
		EuropeanaApi2Results results = ec.searchApi2(query, 3, 0);
		assertTrue(results.getItemsCount() > 0);
        
        for(EuropeanaApi2Item result : results.getAllItems()) {
        	EuropeanaObject eo = ec.getObject(result.getId());
        	assertNotNull(eo);
        	assertNotNull(eo.getAbout());
        	assertFalse(eo.toString().isEmpty());
        	System.out.println(eo.toString());
        }
	}	
}
