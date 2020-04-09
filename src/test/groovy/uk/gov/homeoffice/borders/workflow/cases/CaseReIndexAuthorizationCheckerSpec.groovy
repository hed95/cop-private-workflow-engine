package uk.gov.homeoffice.borders.workflow.cases

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class CaseReIndexAuthorizationCheckerSpec extends Specification {

    def caseReIndexAuthorizationChecker = new CaseReIndexAuthorizationChecker()

    def setup() {
        caseReIndexAuthorizationChecker.engineAdminRoles = ['admin']
    }

    def 'can authorize'() {
        given: 'a user with admin role'
        def authentication = new TestingAuthenticationToken("test", "test",
                Collections.singletonList(new SimpleGrantedAuthority('admin'))
        )

        when: 'checker invoked'
        def result = caseReIndexAuthorizationChecker.isAuthorized(authentication)

        then:
        result
    }

    def 'not authorized'() {
        given: 'a user with non admin role'
        def authentication = new TestingAuthenticationToken("test", "test",
                Collections.singletonList(new SimpleGrantedAuthority('user'))
        )

        when: 'checker invoked'
        def result = caseReIndexAuthorizationChecker.isAuthorized(authentication)

        then:
        !result
    }
}
