package eu.europeana.api.client.search.query

import spock.lang.Specification

/**
 * Unit tests for {@link Api2QueryBuilder}.
 */
class Api2QueryBuilderSpec extends Specification {

    Api2QueryBuilder builder = new Api2QueryBuilder()

    def "buildQuery fills fields and refinements"() {
        when:
        Api2QueryInterface query = builder.buildQuery(
                'my-collection',
                'general',
                'building',
                'picasso',
                'IMAGE',
                'Europeana',
                'Library',
                ['NOT gips', 'NOT capitel'] as String[])

        then:
        query.collectionName == 'my-collection'
        query.generalTerms == 'general'
        query.whatTerms == 'building'
        query.creator == 'picasso'
        query.type == 'IMAGE'
        query.provider == 'Europeana'
        query.dataProvider == 'Library'
        query.queryRefinements == ['NOT gips', 'NOT capitel']
    }

    def "buildQuery ignores null refinements array"() {
        when:
        Api2QueryInterface query = builder.buildQuery('c', 'g', 'w', null, null, null, null, null)

        then:
        query.queryRefinements == null
    }

    def "buildQuery from portal URL renames q to query and drops pagination"() {
        given:
        String portalUrl = 'http://www.europeana.eu/portal/search.html?' +
                'query=DATA_PROVIDER%3A%22The+Wellcome+Library%22+Great+War' +
                '&start=13&rows=12'

        when:
        Api2QueryInterface query = builder.buildQuery(portalUrl)

        then:
        query.queryParams == 'query=DATA_PROVIDER%3A%22The+Wellcome+Library%22+Great+War'
        !query.queryParams.contains('start=')
        !query.queryParams.contains('rows=')
    }

    def "buildQuery from portal URL rewrites leading q parameter to query"() {
        given:
        String portalUrl = 'http://www.europeana.eu/portal/search.html?q=picasso&start=1&rows=12'

        when:
        Api2QueryInterface query = builder.buildQuery(portalUrl)

        then:
        query.queryParams == 'query=picasso'
    }

    def "buildBaseQuery drops wskey and pagination from API URL"() {
        given:
        // wskey must not be the first param: removeParam looks for "&wskey="
        String apiUrl = 'http://www.europeana.eu/api/v2/search.json?' +
                'query=what%3A(building)&wskey=SECRET&start=1&rows=12'

        when:
        Api2QueryInterface query = builder.buildBaseQuery(apiUrl)

        then:
        query.queryParams == 'query=what%3A(building)'
        !query.queryParams.contains('wskey')
        !query.queryParams.contains('start=')
        !query.queryParams.contains('rows=')
    }

    def "buildQuery overload with object type only sets type"() {
        when:
        Api2QueryInterface query = builder.buildQuery('col', 'terms', 'what', 'IMAGE')

        then:
        query.collectionName == 'col'
        query.generalTerms == 'terms'
        query.whatTerms == 'what'
        query.type == 'IMAGE'
        query.creator == null
    }
}
