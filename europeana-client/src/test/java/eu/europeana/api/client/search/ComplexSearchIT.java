package eu.europeana.api.client.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import eu.europeana.api.client.EuropeanaApi2Client;
import eu.europeana.api.client.connection.ApacheHttpConnectors;
import eu.europeana.api.client.exception.EuropeanaApiProblem;
import eu.europeana.api.client.model.EuropeanaApi2Results;
import eu.europeana.api.client.model.search.EuropeanaApi2Item;
import eu.europeana.api.client.search.common.EuropeanaFields;
import eu.europeana.api.client.search.common.EuropeanaOperators;
import eu.europeana.api.client.search.query.EuropeanaComplexQuery;
import eu.europeana.api.client.search.query.adv.EuropeanaOperand;
import eu.europeana.api.client.search.query.adv.EuropeanaSearchTerm;

public class ComplexSearchIT {

    @Test
    public void testComplexSearch() throws IOException, EuropeanaApiProblem{
        
         long ms0 = System.currentTimeMillis();

         //build a complex query
         EuropeanaOperand opA = new EuropeanaOperand("Shakespeare");
         EuropeanaOperand opB = new EuropeanaOperand("William");
         EuropeanaOperand opC = new EuropeanaOperand("poetry");
         EuropeanaOperand opSimple = new EuropeanaOperand("dune");
         EuropeanaOperand opComplex = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB);
         opComplex.addOperand(EuropeanaOperators.AND, opC);
         EuropeanaOperand opComplex2 = new EuropeanaOperand(EuropeanaOperators.OR, opComplex, opSimple);
         EuropeanaSearchTerm stSimple = new EuropeanaSearchTerm(EuropeanaFields.TITLE, opComplex2);
         EuropeanaSearchTerm stComplex = new EuropeanaSearchTerm(EuropeanaFields.TITLE, opSimple);
         stComplex.addSearchTerm(EuropeanaOperators.OR, stSimple);
         
         //OR A QUICK AND SIMPLE SEARCH:
         stComplex = new EuropeanaSearchTerm(EuropeanaFields.CREATOR, "eminescu");
         EuropeanaComplexQuery europeanaQuery = new EuropeanaComplexQuery(stComplex);
         //set query type
         europeanaQuery.setType(EuropeanaComplexQuery.TYPE.TEXT);
         
         //invoke the search api
         EuropeanaApi2Client europeanaClient = new EuropeanaApi2Client(ApacheHttpConnectors.create());
         final int FECTHED_RESULTS_COUNT = 20;
        EuropeanaApi2Results res = europeanaClient.searchApi2(europeanaQuery, FECTHED_RESULTS_COUNT, 0);
         
         long t = System.currentTimeMillis() - ms0;
         System.out.println("*** Response time (client + server processing): " + (t / 1000d) + " seconds");
         System.out.println("Results: " + res.getItemsCount() + " / " + res.getTotalResults());
         
         //Check results
         assertTrue(res.getTotalResults() > 0);
         
         //display results
         if (res.getItemsCount() > 0) {
             List<EuropeanaApi2Item> items = res.getAllItems();
             for (int i = 0; i < items.size(); i++) {
                 EuropeanaApi2Item item = items.get(i);
                 System.out.println();
                 System.out.println("**** " + (i + 1));
                 System.out.println("Title: " + item.getTitle());
                 System.out.println("Europeana URL: " + item.getObjectURL());
                 System.out.println("Type: " + item.getType());
                 System.out.println("Creator: " + item.getDcCreator());
                 System.out.println("Thumbnail: " + item.getEdmPreview());
                 System.out.println("Data provider: "+ item.getDataProvider());
                 System.out.println("Id: "+ item.getId());
                 System.out.println("Guid: "+ item.getGuid());
                 
             }
         }
    }
}
