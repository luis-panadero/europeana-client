package eu.europeana.api.client.util

import eu.europeana.api.client.util.BlockIterator.BlockLoader
import spock.lang.Specification

/**
 * Unit tests for {@link BlockIterator}.
 */
class BlockIteratorSpec extends Specification {

    def "iterates across multiple blocks"() {
        given:
        BlockLoader<String> loader = Stub() {
            getTotalRecords() >> 5L
            loadBlock(0L) >> ['a', 'b', 'c']
            loadBlock(3L) >> ['d', 'e']
        }
        def iterator = new BlockIterator<String>(loader)

        when:
        List<String> values = []
        while (iterator.hasNext()) {
            values << iterator.next()
        }

        then:
        values == ['a', 'b', 'c', 'd', 'e']
    }

    def "hasNext does not consume elements"() {
        given:
        BlockLoader<String> loader = Stub() {
            getTotalRecords() >> 2L
            loadBlock(0L) >> ['a', 'b']
        }
        def iterator = new BlockIterator<String>(loader)

        expect:
        iterator.hasNext()
        iterator.hasNext()
        iterator.next() == 'a'
        iterator.next() == 'b'
        !iterator.hasNext()
    }

    def "next throws NoSuchElementException when exhausted"() {
        given:
        BlockLoader<Integer> loader = Stub() {
            getTotalRecords() >> 0L
        }
        def iterator = new BlockIterator<Integer>(loader)

        when:
        iterator.next()

        then:
        thrown(NoSuchElementException)
    }

    def "remove is not supported"() {
        given:
        BlockLoader<Integer> loader = Stub() {
            getTotalRecords() >> 1L
            loadBlock(_) >> [1]
        }
        def iterator = new BlockIterator<Integer>(loader)

        when:
        iterator.hasNext()
        iterator.remove()

        then:
        thrown(UnsupportedOperationException)
    }

    def "empty block ends iteration early"() {
        given:
        BlockLoader<String> loader = Stub() {
            getTotalRecords() >> 10L
            loadBlock(0L) >> []
        }
        def iterator = new BlockIterator<String>(loader)

        expect:
        !iterator.hasNext()
    }
}
