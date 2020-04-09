package uk.gov.homeoffice.borders.workflow.cases

import spock.lang.Specification

class CaseReIndexControllerSpec extends Specification {

    private CaseReIndexController controller
    private CaseReIndexer reIndexer = Mock()

    def setup() {
        controller = new CaseReIndexController(reIndexer)
    }

    def 'can trigger reindex'() {

        when: 'reindex requested'
        controller.reindex("caseId")

        then:
        1 * reIndexer.reindex("caseId", CaseReIndexer.DEFAULT_LISTENER)
    }
}
