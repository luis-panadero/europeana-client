package eu.europeana.api.client.model

import com.google.gson.Gson
import eu.europeana.api.client.model.search.EuropeanaApi2Item
import spock.lang.Specification

/**
 * Unit tests for {@link EuropeanaApi2Results}.
 */
class EuropeanaApi2ResultsSpec extends Specification {

    def "addItem and getAllItems expose an unmodifiable list"() {
        given:
        def results = new EuropeanaApi2Results()
        def item = new EuropeanaApi2Item(id: '/x/y')

        when:
        results.addItem(item)

        then:
        results.allItems.size() == 1
        results.allItems[0].id == '/x/y'
        results.itemCount == 1

        when:
        results.allItems.add(new EuropeanaApi2Item())

        then:
        thrown(UnsupportedOperationException)
    }

    def "getAllItems returns empty list when items are null"() {
        given:
        def results = new EuropeanaApi2Results()
        results.items = null

        expect:
        results.allItems.isEmpty()
        results.itemCount == 0
    }

    def "acumulate merges items and counts"() {
        given:
        def first = new EuropeanaApi2Results()
        first.itemsCount = 1
        first.addItem(new EuropeanaApi2Item(id: '1'))
        def second = new EuropeanaApi2Results()
        second.itemsCount = 2
        second.addItem(new EuropeanaApi2Item(id: '2'))
        second.addItem(new EuropeanaApi2Item(id: '3'))

        when:
        first.acumulate(second)

        then:
        first.allItems*.id == ['1', '2', '3']
        first.itemsCount == 3
    }

    def "limitResults truncates items and itemsCount"() {
        given:
        def results = new EuropeanaApi2Results()
        3.times { results.addItem(new EuropeanaApi2Item(id: "id-$it")) }
        results.itemsCount = 3

        when:
        results.limitResults(2)

        then:
        results.allItems.size() == 2
        results.itemsCount == 2
        results.allItems*.id == ['id-0', 'id-1']
    }

    def "limitResults is a no-op when under the limit"() {
        given:
        def results = new EuropeanaApi2Results()
        results.addItem(new EuropeanaApi2Item(id: 'only'))
        results.itemsCount = 1

        when:
        results.limitResults(5)

        then:
        results.allItems.size() == 1
        results.itemsCount == 1
    }

    def "toJSON roundtrip preserves counts and item id"() {
        given:
        def original = new EuropeanaApi2Results()
        original.itemsCount = 1
        original.totalResults = 42
        original.nextCursor = 'abc'
        original.addItem(new EuropeanaApi2Item(id: '/a/b', type: 'IMAGE'))
        def gson = new Gson()

        when:
        String json = original.toJSON()
        EuropeanaApi2Results restored = gson.fromJson(json, EuropeanaApi2Results)

        then:
        restored.itemsCount == 1
        restored.totalResults == 42
        restored.nextCursor == 'abc'
        restored.allItems.size() == 1
        restored.allItems[0].id == '/a/b'
        restored.allItems[0].type == 'IMAGE'
    }

    def "inline minimal JSON can be deserialized"() {
        given:
        String json = '''
            {
              "success": true,
              "itemsCount": 1,
              "totalResults": 100,
              "items": [{"id": "/item/1", "type": "TEXT", "title": ["Hello"]}]
            }
        '''

        when:
        EuropeanaApi2Results results = new Gson().fromJson(json, EuropeanaApi2Results)

        then:
        results.itemsCount == 1
        results.totalResults == 100
        results.allItems[0].id == '/item/1'
        results.allItems[0].type == 'TEXT'
        results.allItems[0].title == ['Hello']
    }
}
