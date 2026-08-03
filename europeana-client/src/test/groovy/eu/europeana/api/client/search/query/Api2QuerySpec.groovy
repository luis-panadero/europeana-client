package eu.europeana.api.client.search.query

import eu.europeana.api.client.connection.BaseApiConnection
import spock.lang.Specification

/**
 * Unit tests for {@link Api2Query}.
 */
class Api2QuerySpec extends Specification {

    static final String BASE_URI = 'http://www.europeana.eu/api/v2/'
    static final String API_KEY = 'TESTKEY'

    BaseApiConnection connection = new BaseApiConnection(BASE_URI, API_KEY)

    def "collectionName is included in search terms"() {
        given:
        def query = new Api2Query('"05812_L_RO_CIMEC_ese"')
        query.setWhatTerms('building')

        expect:
        query.getSearchTerms() ==
                'what%3A(building)+AND+europeana_collectionName%3A(%2205812_L_RO_CIMEC_ese%22)'
    }

    def "blank and null refinements are omitted from the URL"() {
        given:
        def query = new Api2Query()
        query.setWhatTerms('building')
        query.addQueryRefinement('NOT gips')
        query.addQueryRefinement(null)
        query.addQueryRefinement('  ')
        query.addQueryRefinement('NOT capitel')

        when:
        String url = query.getQueryUrl(connection, 1, 1)

        then:
        url.contains('&qf=NOT+gips')
        url.contains('&qf=NOT+capitel')
        !url.contains('&qf=&')
        url ==~ /.*&qf=NOT\+gips&qf=NOT\+capitel.*/
    }

    def "getQueryUrl matches SearchRefinements scenario without calling the API"() {
        given:
        def query = new Api2Query('"05812_L_RO_CIMEC_ese"')
        query.setWhatTerms('building')
        query.addQueryRefinement('NOT gips')
        query.addQueryRefinement('NOT capitel')

        when:
        String url = query.getQueryUrl(connection, 1, 1)

        then:
        url == BASE_URI + 'search.json?wskey=' + API_KEY +
                '&query=what%3A(building)+AND+europeana_collectionName%3A(%2205812_L_RO_CIMEC_ese%22)' +
                '&qf=NOT+gips&qf=NOT+capitel&rows=1&start=1'
    }

    def "queryParams bypasses getSearchTerms and refinements"() {
        given:
        def query = new Api2Query()
        query.setWhatTerms('ignored')
        query.addQueryRefinement('NOT gips')
        query.setQueryParams('query=what%3A(building)')

        when:
        String url = query.getQueryUrl(connection, 12, 0)

        then:
        url == BASE_URI + 'search.json?wskey=' + API_KEY + '&query=what%3A(building)&rows=12'
        !url.contains('qf=')
    }

    def "cursor pagination appends cursor and rows"() {
        given:
        def query = new Api2Query()
        query.setWhatTerms('building')

        when:
        String url = query.getQueryUrl(connection, '*', 10)

        then:
        url == BASE_URI + 'search.json?wskey=' + API_KEY +
                '&query=what%3A(building)&cursor=*&rows=10'
    }

    def "profile is included in the base search URL when set"() {
        given:
        def query = new Api2Query()
        query.setWhatTerms('building')
        query.setProfile('rich')

        when:
        String url = query.getQueryUrl(connection, 5, 1)

        then:
        url.startsWith(BASE_URI + 'search.json?wskey=' + API_KEY + '&profile=rich&query=')
        url.endsWith('&rows=5&start=1')
    }

    def "limit and offset zero omit rows and start"() {
        given:
        def query = new Api2Query()
        query.setWhatTerms('building')

        when:
        String url = query.getQueryUrl(connection, 0, 0)

        then:
        url == BASE_URI + 'search.json?wskey=' + API_KEY + '&query=what%3A(building)'
        !url.contains('rows=')
        !url.contains('start=')
    }
}
