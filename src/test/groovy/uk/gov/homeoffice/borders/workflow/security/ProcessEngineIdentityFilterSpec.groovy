package uk.gov.homeoffice.borders.workflow.security

import org.camunda.bpm.engine.IdentityService
import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.representations.AccessToken
import org.springframework.util.AntPathMatcher
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser
import uk.gov.homeoffice.borders.workflow.identity.UserQuery

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ProcessEngineIdentityFilterSpec extends Specification {

    def identityService = Mock(IdentityService)
    def antPathMatcher = new AntPathMatcher()

    def request = Mock(HttpServletRequest)
    def response = Mock(HttpServletResponse)
    def chain = Mock(FilterChain)


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

        and:
        def underTest = new ProcessEngineIdentityFilter(identityService,refreshableKeycloakSecurityContext, antPathMatcher)

        when:
        underTest.doFilterInternal(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        1 * identityService.clearAuthentication()
        authentication.user
        authentication.user.email == 'service-email'

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

        def user = new PlatformUser()
        user.id ='id'
        user.email = 'email'
        user.teams = []

        and:
        def userQuery = Mock(UserQuery)
        identityService.createUserQuery() >> userQuery
        userQuery.userId("email") >> userQuery
        userQuery.singleResult() >> user

        and:
        def underTest = new ProcessEngineIdentityFilter(identityService,refreshableKeycloakSecurityContext, antPathMatcher)

        when:
        underTest.doFilterInternal(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        authentication.user
        authentication.user.email == 'email'
        1 * identityService.clearAuthentication()
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
        def underTest = new ProcessEngineIdentityFilter(identityService,refreshableKeycloakSecurityContext, antPathMatcher)


        when:
        underTest.doFilterInternal(request, response, chain)

        then:
        1 * identityService.setAuthentication(_)  >> { arguments -> authentication=arguments[0]}
        1 * identityService.clearAuthentication()
        authentication.user
        authentication.userId == 'email'
        authentication.getGroupIds().size() == 0
    }

}
