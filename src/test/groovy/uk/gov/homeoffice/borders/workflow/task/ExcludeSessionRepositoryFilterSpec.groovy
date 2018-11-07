package uk.gov.homeoffice.borders.workflow.task

import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

class ExcludeSessionRepositoryFilterSpec extends Specification {


    def filter = new ExcludeSessionRepositoryFilter()

    def 'can update request if /rest/camunda/external-task'() {
        given:
        def request = new MockHttpServletRequest()
        request.setServletPath("/rest/camunda/external-task/fetchAndLock")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()


        when:
        filter.doFilterInternal(request, response, chain)

        then:
        request.getAttribute("org.springframework.session.web.http.SessionRepositoryFilter.FILTERED") == Boolean.TRUE
    }

    def 'request not updated if path does not match'() {
        given:
        def request = new MockHttpServletRequest()
        request.setServletPath("/rest/camunda/process-instance")
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()


        when:
        filter.doFilterInternal(request, response, chain)

        then:
        !request.getAttribute("org.springframework.session.web.http.SessionRepositoryFilter.FILTERED")
    }
}
