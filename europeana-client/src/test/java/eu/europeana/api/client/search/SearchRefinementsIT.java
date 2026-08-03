package eu.europeana.api.client.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.exception.EuropeanaApiProblem;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.model.search.EuropeanaApi2Item;
import eu.europeana.api.client.search.query.Api2Query;

public class SearchRefinementsIT {

	@Test
	public void testQueryRefinements() throws IOException, EuropeanaApiProblem {

		long ms0 = System.currentTimeMillis();

		// create the query object
		Api2Query europeanaQuery = new Api2Query("\"2020706_Ag_EU_CARARE_NPU\"");
		europeanaQuery.setWhatTerms("building");
		europeanaQuery.addQueryRefinement("NOT gips");
		europeanaQuery.addQueryRefinement("NOT capitel");

		EuropeanaApi2Client europeanaClient = new EuropeanaApi2Client();
		final int RESULTS_SIZE = 1;
		final int OFFSET = 1;
		String queryUrl = europeanaQuery.getQueryUrl(europeanaClient,
				RESULTS_SIZE, OFFSET);
		// System.out.println(queryUrl);
		String encodedUrl = "https://api.europeana.eu/api/v2/search.json?wskey="
				+ europeanaClient.getApiKey()
				+ "&query=what%3A(building)+AND+europeana_collectionName%3A(%222020706_Ag_EU_CARARE_NPU%22)"
				+ "&qf=NOT+gips&qf=NOT+capitel&rows=1&start=1";

		assertEquals(encodedUrl, queryUrl);

		// perform search
		EuropeanaApi2Results res = europeanaClient.searchApi2(europeanaQuery,
				RESULTS_SIZE, OFFSET);

		// print out response time
		long t = System.currentTimeMillis() - ms0;
		System.out.println("response time (client+server): " + (t / 1000d)
				+ " seconds");

		assertEquals(RESULTS_SIZE, res.getItemsCount());
		// Old dataset 05812_L_RO_CIMEC_ese no longer returns results; assert presence only.
		assertTrue(res.getTotalResults() > 0);

		int count = 0;
		for (EuropeanaApi2Item item : res.getAllItems()) {
			System.out.println();
			System.out.println("**** " + (count++ + 1));
			System.out.println("Title: " + item.getTitle());
			System.out.println("Europeana URL: " + item.getObjectURL());
			System.out.println("Type: " + item.getType());
			System.out.println("Creator(s): " + item.getDcCreator());
			System.out.println("Thumbnail(s): " + item.getEdmPreview());
			System.out.println("Data provider: " + item.getDataProvider());
		}
	}
}
