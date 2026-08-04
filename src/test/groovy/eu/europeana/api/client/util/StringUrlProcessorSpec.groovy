package eu.europeana.api.client.util

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for {@link StringUrlProcessor}.
 */
class StringUrlProcessorSpec extends Specification {

    StringUrlProcessor processor = new StringUrlProcessor()

    @Unroll
    def "removeParam: #label"() {
        expect:
        processor.removeParam(queryParam, urlPart) == expected

        where:
        label              | queryParam  | urlPart                      | expected
        'middle param'     | '&start='   | 'a=1&start=5&rows=10'        | 'a=1&rows=10'
        'last param'       | '&rows='    | 'a=1&start=5&rows=10'        | 'a=1&start=5'
        'absent param'     | '&wskey='   | 'a=1&start=5&rows=10'        | 'a=1&start=5&rows=10'
        'only param'       | 'start='    | 'start=5'                    | ''
        'wskey in middle'  | '&wskey='   | 'query=x&wskey=KEY&rows=10'  | 'query=x&rows=10'
        'start then rows'  | '&start='   | 'query=x&start=13&rows=12'   | 'query=x&rows=12'
    }

    @Unroll
    def "replaceParam: #label"() {
        expect:
        processor.replaceParam(queryParam, value, urlPart) == expected

        where:
        label                    | queryParam | value | urlPart           | expected
        'param name with ='      | '&rows='   | '20'  | 'a=1&rows=10'     | 'a=1&rows=20'
        'param name without ='   | '&rows'    | '20'  | 'a=1&rows=10'     | 'a=1&rows=20'
        'absent then append'     | '&start='  | '1'   | 'a=1&rows=10'     | 'a=1&rows=10&start=1'
        'replace last param'     | '&rows='   | '5'   | 'query=x&rows=12' | 'query=x&rows=5'
    }

    def "removeParam chain used by Api2QueryBuilder for pagination"() {
        given:
        String params = 'query=DATA_PROVIDER%3A%22X%22&start=13&rows=12'

        when:
        String withoutStart = processor.removeParam('&start=', params)
        String withoutRows = processor.removeParam('&rows=', withoutStart)

        then:
        withoutRows == 'query=DATA_PROVIDER%3A%22X%22'
    }
}
