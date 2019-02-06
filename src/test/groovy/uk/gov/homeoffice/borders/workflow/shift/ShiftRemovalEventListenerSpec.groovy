package uk.gov.homeoffice.borders.workflow.shift

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.spin.Spin
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import spock.lang.Specification
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser

class ShiftRemovalEventListenerSpec extends Specification {

    def cacheManager = Mock(CacheManager)
    def formatter = new JacksonJsonDataFormat("application/json", new ObjectMapper())

    def shiftRemovalEventListener = new ShiftRemovalEventListener(cacheManager, formatter)


    def 'can notify shift cache removal'() {
        given:
        def execution = Mock(DelegateExecution)
        def processInstanceExecution = Mock(DelegateExecution)
        execution.getProcessInstance() >> processInstanceExecution
        processInstanceExecution.isCanceled() >> false
        execution.getCurrentActivityName() >> "Remove shift record"
        def shiftInfo = new PlatformUser.ShiftDetails()
        shiftInfo.email = 'email'
        def typeValue = new ObjectValueImpl(Spin.S(shiftInfo, formatter))
        execution.getVariableTyped("shiftInfo", true) >> typeValue

        def cache = Mock(Cache)
        cacheManager.getCache("shifts") >> cache

        when:
        shiftRemovalEventListener.notify(execution)

        then:
        1 * cache.evict('email')
    }
}
