package eu.europeana.api.client.search.query.adv

import eu.europeana.api.client.search.common.EuropeanaFields
import eu.europeana.api.client.search.common.EuropeanaOperators
import spock.lang.Specification

/**
 * Unit tests for {@link EuropeanaSearchTerm}.
 */
class EuropeanaSearchTermSpec extends Specification {

    def "simple search term formats as field colon operand"() {
        given:
        def term = new EuropeanaSearchTerm(EuropeanaFields.what, new EuropeanaOperand('picasso'))

        expect:
        term.toString() == 'what: picasso'
    }

    def "string constructor builds the same simple term"() {
        expect:
        new EuropeanaSearchTerm(EuropeanaFields.who, 'eminescu').toString() == 'who: eminescu'
    }

    def "addSearchTerm composes terms without leading operator on the first"() {
        given:
        def first = new EuropeanaSearchTerm(EuropeanaFields.what, 'picasso')
        def second = new EuropeanaSearchTerm(EuropeanaFields.what, 'dune')

        when:
        first.addSearchTerm(EuropeanaOperators.OR, second)

        then:
        first.toString() == 'what: picasso OR what: dune'
    }

    def "complex tree inspired by ComplexSearchIT"() {
        given:
        def opA = new EuropeanaOperand('Shakespeare')
        def opB = new EuropeanaOperand('William')
        def opC = new EuropeanaOperand('poetry')
        def opSimple = new EuropeanaOperand('dune')
        def opComplex = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB)
        opComplex.addOperand(EuropeanaOperators.AND, opC)
        def opComplex2 = new EuropeanaOperand(EuropeanaOperators.OR, opComplex, opSimple)
        def stSimple = new EuropeanaSearchTerm(EuropeanaFields.TITLE, opComplex2)
        def stComplex = new EuropeanaSearchTerm(EuropeanaFields.TITLE, opSimple)

        when:
        stComplex.addSearchTerm(EuropeanaOperators.OR, stSimple)

        then:
        // EuropeanaFields.TITLE aliases to "what"
        stComplex.toString() == 'what: dune OR what: ((Shakespeare AND William AND poetry) OR dune)'
    }

    def "copy constructor preserves composed search terms"() {
        given:
        def original = new EuropeanaSearchTerm(EuropeanaFields.what, 'picasso')
        original.addSearchTerm(EuropeanaOperators.OR, new EuropeanaSearchTerm(EuropeanaFields.what, 'dune'))

        when:
        def copy = new EuropeanaSearchTerm(original)

        then:
        copy.toString() == original.toString()
        !copy.is(original)
    }
}
