package uk.gov.homeoffice.borders.workflow

import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.TeamQuery

class PlatformDataUrlBuilderSpec extends Specification {

    def platformDataUrl = 'http://localhost:9000'

    def underTest = new PlatformDataUrlBuilder(platformDataUrl)

    def 'can get shift url by email'() {
        given:
        def email = "myemail@host.com"

        when:
        def url = underTest.shiftUrlByEmail(email)

        then:
        url
        url == 'http://localhost:9000/shift?email=eq.myemail%40host.com'

    }

    def 'can get shift url by id'() {
        given:
        def id = 'uuid'

        when:
        def url = underTest.shiftUrlById(id)

        then:
        url
        url == 'http://localhost:9000/shift?shiftid=eq.uuid'
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

    def 'can get team url by team name' () {
        given:
        def teamName = "teamName"
        def teamQuery = new TeamQuery().groupName(teamName)

        when:
        def url = underTest.teamQuery(teamQuery)

        then:
        url
        url == "http://localhost:9000/team?teamname=eq.teamName"
    }


    def 'can get team url by team name like' () {
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

    def 'can get shift url by team id'() {
        given:
        def teamId = "teamId"

        when:
        def url = underTest.queryShiftByTeamId(teamId)

        then:
        url
        url == 'http://localhost:9000/shift?teamid=eq.teamId'
    }

    def 'can get shift url by location id'() {
        given:
        def locationId = "locationId"

        when:
        def url = underTest.queryShiftByLocationId(locationId)

        then:
        url
        url == 'http://localhost:9000/shift?locationid=eq.locationId'
    }

    def 'can get shift url by command id'() {
        given:
        def commandId = "commandId"

        when:
        def url = underTest.queryShiftByCommandId(commandId)

        then:
        url
        url == 'http://localhost:9000/shift?or=(subcommandid.eq.commandId,commandid.eq.commandId)'
    }

    def 'can get staff url'() {
        given:
        def staffId = "staffId"

        when:
        def url = underTest.getStaffUrl(staffId)

        then:
        url
        url == 'http://localhost:9000/staffview?staffid=eq.staffId'
    }

    def 'can get team children url'() {
        when:
        def url = underTest.teamChildren()

        then:
        url
        url == 'http://localhost:9000/rpc/teamchildren'
    }

    def 'can get url for staff view in'() {
        given:
        def staffIds = ['staffid1', 'staffid2']

        when:
        def url = underTest.staffViewIn(staffIds)

        then:
        url
        url == "http://localhost:9000/staffview?staffid=in.(staffid1,staffid2)"
    }

    def 'can get url for comments'() {
        when:
        def url = underTest.comments()

        then:
        url
        url == 'http://localhost:9000/taskcomment'
    }

    def 'can get url for comments for task'()  {
        given:
        def taskId = 'taskId'

        when:
        def url = underTest.getCommentsById(taskId)

        then:
        url
        url == 'http://localhost:9000/taskcomment?taskid=eq.taskId'
    }

    def 'can get shift url by subcommand id'() {
        given:
        def subCommandId = "subcommandId"

        when:
        def url = underTest.queryShiftBySubCommandId(subCommandId)

        then:
        url
        url == 'http://localhost:9000/shift?subcommandid=eq.subcommandId'
    }

    def 'can get shift url by email endcoded'() {
        given:
        def email = "my+email+abc@host.com"

        when:
        def url = underTest.shiftUrlByEmail(email)

        then:
        url
        url == 'http://localhost:9000/shift?email=eq.my%2Bemail%2Babc%40host.com'

    }

}
