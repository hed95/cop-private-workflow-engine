package uk.gov.homeoffice.borders.workflow.identity;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.shift.ShiftInfo;

import static org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END;

@Slf4j
@Component
@CamundaSelector(type = ActivityTypes.TASK_SERVICE, event = EVENTNAME_END)
public class ShiftRemovalEventListener extends ReactorExecutionListener {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (execution.getCurrentActivityName().equalsIgnoreCase("Remove shift record")) {
            ShiftInfo shiftInfo = (ShiftInfo) execution.
                    getVariableTyped("shiftInfo", true)
                    .getValue();
            String email = shiftInfo.getEmail();
            cacheManager.getCache("shifts").evict(email);
            log.info("Shift info for '{}' removed from cache", email);
        }
    }
}
