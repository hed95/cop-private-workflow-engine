package uk.gov.homeoffice.borders.workflow


import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.config.RefDataBean
import uk.gov.homeoffice.borders.workflow.identity.TeamQuery

class RefDataUrlBuilderSpec extends Specification {

    def refDataUrl = 'http://localhost:9000'
    def refDataBean = new RefDataBean()

    def underTest

    def setup() {
        refDataBean.url = refDataUrl
        underTest = new RefDataUrlBuilder(refDataBean)
    }

    def 'can get team url for team id'() {
        given:
        def teamId = "teamId"
        def teamQuery = new TeamQuery().groupId(teamId)

        when:
        def url = underTest.teamQuery(teamQuery)

        then:
        url
        url == "http://localhost:9000/team?teamid=eq.teamId"
    }

    def 'can get team url by team name'() {
        given:
        def teamName = "teamName"
        def teamQuery = new TeamQuery().groupName(teamName)

        when:
        def url = underTest.teamQuery(teamQuery)

        then:
        url
        url == "http://localhost:9000/team?teamname=eq.teamName"
    }


    def 'can get team url by team name like'() {
        given:
        def teamName = "teamName"
        def teamQuery = new TeamQuery().groupNameLike(teamName)

        when:
        def url = underTest.teamQuery(teamQuery)

        then:
        url
        url == "http://localhost:9000/team?teamname=like.teamName"
    }

    def 'can get team url with team ids in'() {
        given:
        def teamId = "teamId"
        def teamQuery = new TeamQuery().groupIdIn(teamId)

        when:
        def url = underTest.teamQuery(teamQuery)

        then:
        url
        url == "http://localhost:9000/team?teamid=in.(\"teamId\")"
    }

    def 'can get team children url'() {
        when:
        def url = underTest.teamChildren()

        then:
        url
        url == 'http://localhost:9000/rpc/teamchildren'
    }
}
