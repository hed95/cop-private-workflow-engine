package uk.gov.homeoffice.borders.workflow.identity

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.homeoffice.borders.workflow.BaseSpec

class IdentityHelperSpec extends BaseSpec {

    @Autowired
    IdentityHelper identityHelper

    def 'can get groups from current user'() {
        given:
        logInUser()

        when:
        def groupsAsString = identityHelper.candidateGroupsForCurrentUser()

        then:
        groupsAsString == 'teamA'
    }

}
