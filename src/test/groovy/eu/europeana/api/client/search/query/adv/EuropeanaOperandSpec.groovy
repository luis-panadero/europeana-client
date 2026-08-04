package eu.europeana.api.client.search.query.adv

import eu.europeana.api.client.search.common.EuropeanaOperators
import spock.lang.Specification

/**
 * Unit tests for {@link EuropeanaOperand}.
 */
class EuropeanaOperandSpec extends Specification {

    def "simple operand toString is the raw value"() {
        expect:
        new EuropeanaOperand('Shakespeare').toString() == 'Shakespeare'
    }

    def "forceQuotes wraps the value in quotes"() {
        expect:
        new EuropeanaOperand('Shakespeare', true).toString() == '"Shakespeare"'
    }

    def "binary constructor joins operands with operator inside parentheses"() {
        given:
        def opA = new EuropeanaOperand('Shakespeare')
        def opB = new EuropeanaOperand('William')

        when:
        def complex = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB)

        then:
        complex.toString() == '(Shakespeare AND William)'
    }

    def "addOperand appends another operand with its operator"() {
        given:
        def opA = new EuropeanaOperand('Shakespeare')
        def opB = new EuropeanaOperand('William')
        def opC = new EuropeanaOperand('poetry')
        def complex = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB)

        when:
        complex.addOperand(EuropeanaOperators.AND, opC)

        then:
        complex.toString() == '(Shakespeare AND William AND poetry)'
    }

    def "nested OR of complex AND and simple operand"() {
        given:
        def opA = new EuropeanaOperand('Shakespeare')
        def opB = new EuropeanaOperand('William')
        def opC = new EuropeanaOperand('poetry')
        def opSimple = new EuropeanaOperand('dune')
        def opComplex = new EuropeanaOperand(EuropeanaOperators.AND, opA, opB)
        opComplex.addOperand(EuropeanaOperators.AND, opC)

        when:
        def nested = new EuropeanaOperand(EuropeanaOperators.OR, opComplex, opSimple)

        then:
        nested.toString() == '((Shakespeare AND William AND poetry) OR dune)'
    }

    def "copy constructor deep-copies operand tree"() {
        given:
        def original = new EuropeanaOperand(EuropeanaOperators.AND,
                new EuropeanaOperand('a'), new EuropeanaOperand('b'))

        when:
        def copy = new EuropeanaOperand(original)

        then:
        copy.toString() == original.toString()
        !copy.is(original)
    }
}
