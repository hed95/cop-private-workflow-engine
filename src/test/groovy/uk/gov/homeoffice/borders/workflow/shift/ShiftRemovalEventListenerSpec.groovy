package uk.gov.homeoffice.borders.workflow.shift

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import spock.lang.Specification

class ShiftRemovalEventListenerSpec extends Specification {

    def cacheManager = Mock(CacheManager)

    def shiftRemovalEventListener = new ShiftRemovalEventListener(cacheManager)


    def 'can notify shift cache removal'() {
        given:
        def execution = Mock(DelegateExecution)
        def processInstanceExecution = Mock(DelegateExecution)
        execution.getProcessInstance() >> processInstanceExecution
        processInstanceExecution.isCanceled() >> false
        execution.getCurrentActivityName() >> "Remove shift record"
        def shiftInfo = new ShiftInfo()
        shiftInfo.email = 'email'
        def typeValue = new ObjectValueImpl(shiftInfo)
        execution.getVariableTyped("shiftInfo", true) >> typeValue

        def cache = Mock(Cache)
        cacheManager.getCache("shifts") >> cache

        when:
        shiftRemovalEventListener.notify(execution)

        then:
        1 * cache.evict('email')
    }
}
