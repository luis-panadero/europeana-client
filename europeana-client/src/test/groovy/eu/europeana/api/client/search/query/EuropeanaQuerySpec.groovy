package eu.europeana.api.client.search.query

import spock.lang.Specification

/**
 * Unit tests for {@link EuropeanaQuery}.
 */
class EuropeanaQuerySpec extends Specification {

    def "getSearchTerms builds creator, type and notProvider like SimpleSearchIT"() {
        given:
        def query = new EuropeanaQuery()
        query.setCreator('picasso')
        query.setType(EuropeanaComplexQuery.TYPE.IMAGE)
        query.setNotProvider('Hispana')

        expect:
        query.getSearchTerms() == 'who%3A(picasso)+AND+TYPE%3A(IMAGE)NOT+PROVIDER%3A(%22Hispana%22)'
    }

    def "null and blank fields are omitted from search terms"() {
        given:
        def query = new EuropeanaQuery()
        query.setCreator(null)
        query.setTitle('  ')
        query.setWhatTerms('building')

        expect:
        query.getSearchTerms() == 'what%3A(building)'
    }

    def "wholeSubQuery is used as-is when set"() {
        given:
        def query = new EuropeanaQuery('what:(building)')

        expect:
        query.getSearchTerms() == 'what:(building)'
    }

    def "SubQuery with encode and quotes flags is respected"() {
        given:
        def query = new EuropeanaQuery()
        query.addSubQuery(new SubQuery('TYPE', 'IMAGE', false, false, false))
        query.addSubQuery(new SubQuery('PROVIDER', 'Hispana', true, true, true))

        expect:
        query.getSearchTerms() == 'TYPE%3A(IMAGE)NOT+PROVIDER%3A(%22Hispana%22)'
    }

    def "spaces and colons in values are URL-encoded when encodeValue is true"() {
        given:
        def query = new EuropeanaQuery()
        query.setGeneralTerms('great war')

        expect:
        query.getSearchTerms() == 'text%3A(great+war)'
    }

    def "getQueryString delegates to getSearchTerms"() {
        given:
        def query = new EuropeanaQuery()
        query.setWhatTerms('building')

        expect:
        query.getQueryString() == query.getSearchTerms()
    }

    def "getQueryUrl builds Search API v2 URL"() {
        given:
        def connection = new eu.europeana.api.client.connection.BaseApiConnection(
                'https://api.europeana.eu/api/v2/', 'TESTKEY')
        def query = new EuropeanaQuery()
        query.setWhatTerms('building')

        when:
        String url = query.getQueryUrl(connection, 12, 1)

        then:
        url.startsWith('https://api.europeana.eu/api/v2/search.json?wskey=TESTKEY&query=')
        url.contains('&rows=12')
        url.contains('&start=1')
        !url.contains('opensearch')
        !url.contains('searchTerms=')
        !url.contains('startPage=')
    }
}
