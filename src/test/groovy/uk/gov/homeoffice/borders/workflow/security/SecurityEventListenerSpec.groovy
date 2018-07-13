package uk.gov.homeoffice.borders.workflow.security

import org.camunda.bpm.engine.IdentityService
import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.representations.AccessToken
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.identity.UserQuery

class SecurityEventListenerSpec extends Specification {

    def identityService = Mock(IdentityService)

    def underTest = new SecurityEventListener(identityService)


    def 'can register authenticated service role user into identity service'() {
        WorkflowAuthentication authentication
        given:
        def successEvent = Mock(InteractiveAuthenticationSuccessEvent)
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
        successEvent.getSource() >> keycloakAuthenticationToken
        keycloakAuthenticationToken.getDetails() >> keycloakAccount
        keycloakAccount.getKeycloakSecurityContext() >> refreshableKeycloakSecurityContext

        def token = Mock(AccessToken)
        refreshableKeycloakSecurityContext.getToken() >> token
        def realmAccess = Mock(AccessToken.Access)
        token.getRealmAccess() >> realmAccess
        token.getEmail() >> 'service-email'
        realmAccess.getRoles() >> ['service_role']

        when:
        underTest.onSuccessfulAuthentication(successEvent)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        authentication.userId == 'service-email'
        !authentication.user
    }

    def 'can register user with shift details'() {
        WorkflowAuthentication authentication
        given:
        def successEvent = Mock(InteractiveAuthenticationSuccessEvent)
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
        successEvent.getSource() >> keycloakAuthenticationToken
        keycloakAuthenticationToken.getDetails() >> keycloakAccount
        keycloakAccount.getKeycloakSecurityContext() >> refreshableKeycloakSecurityContext

        def token = Mock(AccessToken)
        refreshableKeycloakSecurityContext.getToken() >> token
        def realmAccess = Mock(AccessToken.Access)
        token.getRealmAccess() >> realmAccess
        token.getEmail() >> 'email'
        realmAccess.getRoles() >> ['platform']

        def user = new ShiftUser()
        user.id ='id'
        user.email = 'email'
        user.teams = []

        and:
        def userQuery = Mock(UserQuery)
        identityService.createUserQuery() >> userQuery
        userQuery.userId("email") >> userQuery
        userQuery.singleResult() >> user

        when:
        underTest.onSuccessfulAuthentication(successEvent)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        authentication.user
        authentication.user.email == 'email'
    }

    def 'can register user without shift details'() {
        WorkflowAuthentication authentication
        given:
        def successEvent = Mock(InteractiveAuthenticationSuccessEvent)
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
        successEvent.getSource() >> keycloakAuthenticationToken
        keycloakAuthenticationToken.getDetails() >> keycloakAccount
        keycloakAccount.getKeycloakSecurityContext() >> refreshableKeycloakSecurityContext

        def token = Mock(AccessToken)
        refreshableKeycloakSecurityContext.getToken() >> token
        def realmAccess = Mock(AccessToken.Access)
        token.getRealmAccess() >> realmAccess
        token.getEmail() >> 'email'
        realmAccess.getRoles() >> ['platform']


        and:
        def userQuery = Mock(UserQuery)
        identityService.createUserQuery() >> userQuery
        userQuery.userId("email") >> userQuery
        userQuery.singleResult() >> null

        when:
        underTest.onSuccessfulAuthentication(successEvent)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        !authentication.user
        authentication.userId == 'email'
        authentication.getGroupIds().size() == 0
    }

}
