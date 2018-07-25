package uk.gov.homeoffice.borders.workflow.identity

import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider
import spock.lang.Specification

class CustomIdentityProviderFactorySpec extends Specification {

    CustomIdentityProviderFactory underTest;
    CustomIdentityProvider customIdentityProvider = Mock(CustomIdentityProvider)

    def setup() {
        underTest = new CustomIdentityProviderFactory(customIdentityProvider)
    }

    def 'can get session type'() {
        when:
        def result = underTest.getSessionType()

        then:
        result == ReadOnlyIdentityProvider
    }

    def 'can open session'() {
        when:
        def result = underTest.openSession()

        then:
        result == customIdentityProvider
    }

}
