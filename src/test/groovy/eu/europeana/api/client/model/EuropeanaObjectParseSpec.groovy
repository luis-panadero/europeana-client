package eu.europeana.api.client.model

import com.google.gson.GsonBuilder

import eu.europeana.api.client.EuropeanaApi2Client
import eu.europeana.api.client.connection.HttpConnector
import eu.europeana.api.client.model.search.EuropeanaObject
import eu.europeana.api.client.model.search.parts.Proxy

import spock.lang.Specification

/**
 * Parses a Europeana record JSON fixture and verifies Proxy DC fields.
 */
class EuropeanaObjectParseSpec extends Specification {

    def "parses record JSON into EuropeanaObject with Proxy DC fields"() {
        given:
        def json = getClass().getResourceAsStream(
                '/eu/europeana/api/client/model/sample_record_response.json').text
        def gson = new GsonBuilder().create()

        when:
        EuropeanaObjectResponse response = gson.fromJson(json, EuropeanaObjectResponse)
        EuropeanaObject object = response.object
        Proxy providerProxy = object.proxies.find { !it.europeanaProxy }

        then:
        object.about == '/test/1'
        object.title == ['Sample Europeana Title']
        providerProxy != null
        providerProxy.dcTitle.def == ['Sample Europeana Title']
        providerProxy.dcCreator.def == ['Sample Creator']
        providerProxy.dcSubject.def == ['Literature']
        providerProxy.dcLanguage.def == ['spa']
        providerProxy.dcDate.def == ['1901']
        providerProxy.dcPublisher.def == ['Sample Publisher']
        object.aggregations[0].edmIsShownBy == 'https://example.org/object.pdf'
        object.europeanaAggregation.edmLandingPage == 'https://www.europeana.eu/item/test/1'
    }

    def "getObject builds record URL from client base URI"() {
        given:
        String capturedUrl = null
        def fixture = getClass().getResourceAsStream(
                '/eu/europeana/api/client/model/sample_record_response.json').text
        def http = new HttpConnector(
                { url -> capturedUrl = url; return fixture },
                { u, n, v -> null },
                { u, o, m -> false })
        def client = new EuropeanaApi2Client('https://api.europeana.eu/api/v2/', 'test-key', http)

        when:
        EuropeanaObject object = client.getObject('/test/1')

        then:
        object.about == '/test/1'
        capturedUrl.startsWith('https://api.europeana.eu/api/v2/record/test/1.json')
        capturedUrl.contains('wskey=test-key')
    }
}
