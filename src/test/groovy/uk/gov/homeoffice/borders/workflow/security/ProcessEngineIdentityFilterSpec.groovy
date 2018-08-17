package uk.gov.homeoffice.borders.workflow.security

import org.camunda.bpm.engine.IdentityService
import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.representations.AccessToken
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.ForbiddenException
import uk.gov.homeoffice.borders.workflow.identity.ShiftUser
import uk.gov.homeoffice.borders.workflow.identity.UserQuery

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class ProcessEngineIdentityFilterSpec extends Specification {

    def identityService = Mock(IdentityService)

    def underTest = new ProcessEngineIdentityFilter(identityService)

    def request = Mock(ServletRequest)
    def response = Mock(ServletResponse)
    def chain = Mock(FilterChain)

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def 'can register authenticated service role user into identity service'() {
        WorkflowAuthentication authentication
        given:
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
        keycloakAuthenticationToken.getDetails() >> keycloakAccount
        keycloakAccount.getKeycloakSecurityContext() >> refreshableKeycloakSecurityContext

        def token = Mock(AccessToken)
        refreshableKeycloakSecurityContext.getToken() >> token
        def realmAccess = Mock(AccessToken.Access)
        token.getRealmAccess() >> realmAccess
        token.getEmail() >> 'service-email'
        realmAccess.getRoles() >> ['service_role']


        def securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.setAuthentication(keycloakAuthenticationToken)
        SecurityContextHolder.setContext(securityContext)

        when:
        underTest.doFilter(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        authentication.userId == 'service-email'
        !authentication.user
    }

    def 'can register user with shift details'() {
        WorkflowAuthentication authentication
        given:
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
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

        and:
        def securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.setAuthentication(keycloakAuthenticationToken)
        SecurityContextHolder.setContext(securityContext)


        when:
        underTest.doFilter(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        authentication.user
        authentication.user.email == 'email'
    }

    def 'can register user without shift details'() {
        WorkflowAuthentication authentication
        given:
        def keycloakAccount = Mock(SimpleKeycloakAccount)
        def keycloakAuthenticationToken = Mock(KeycloakAuthenticationToken)
        def refreshableKeycloakSecurityContext =Mock(RefreshableKeycloakSecurityContext)
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

        and:
        def securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.setAuthentication(keycloakAuthenticationToken)
        SecurityContextHolder.setContext(securityContext)


        when:
        underTest.doFilter(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        !authentication.user
        authentication.userId == 'email'
        authentication.getGroupIds().size() == 0
    }

    def 'throws exception if context is null'() {
        when:
        underTest.doFilter(request, response, chain)

        then:
        thrown(ForbiddenException)
    }

}
