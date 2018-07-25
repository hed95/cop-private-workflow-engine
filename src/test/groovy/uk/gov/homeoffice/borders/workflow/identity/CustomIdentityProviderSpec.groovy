package uk.gov.homeoffice.borders.workflow.identity

import spock.lang.Specification

class CustomIdentityProviderSpec extends Specification {

    UserService userService = Mock(UserService)
    TeamService teamService = Mock(TeamService)

    CustomIdentityProvider underTest = new CustomIdentityProvider(userService, teamService)

    def 'can find by user id'() {
        when:
        underTest.findUserById("userId")

        then:
        1 * userService.findByUserId("userId")
    }

    def 'can create user query command context'() {
        when:
        def userQuery = underTest.createUserQuery(null)

        then:
        userQuery
    }

    def 'cannot create native query'() {
        when:
        underTest.createNativeUserQuery()

        then:
        thrown(UnsupportedOperationException)
    }

    def 'cannot check password'() {
        when:
        underTest.checkPassword("test", "test")

        then:
        thrown(UnsupportedOperationException)
    }

    def 'can find by group id'() {
        given:
        def team = new Team()
        team.name = "team"
        and:
        teamService.findById("id") >> team

        when:
        def result = underTest.findGroupById("id")

        then:
        result == team
    }

}