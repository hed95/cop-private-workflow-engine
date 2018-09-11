package uk.gov.homeoffice.borders.workflow.shift;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.extension.reactor.bus.CamundaSelector;
import org.camunda.bpm.extension.reactor.spring.listener.ReactorExecutionListener;
import org.camunda.spin.Spin;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.shift.ShiftInfo;
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification;

import static org.camunda.bpm.engine.impl.pvm.PvmEvent.EVENTNAME_END;

@Slf4j
@Component
@CamundaSelector(type = ActivityTypes.TASK_SERVICE, event = EVENTNAME_END)
public class ShiftRemovalEventListener extends ReactorExecutionListener {

    private CacheManager cacheManager;
    private JacksonJsonDataFormat formatter;

    @Autowired
    public ShiftRemovalEventListener(CacheManager cacheManager,
                                     JacksonJsonDataFormat formatter) {
        this.cacheManager = cacheManager;
        this.formatter = formatter;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (!execution.getProcessInstance().isCanceled() &&
                execution.getCurrentActivityName().equalsIgnoreCase("Remove shift record")) {

            TypedValue notification = execution.
                    getVariableTyped("shiftInfo", true);

            ShiftInfo shiftInfo = Spin.S(notification.getValue(), formatter).mapTo(ShiftInfo.class);

            String email = shiftInfo.getEmail();
            cacheManager.getCache("shifts").evict(email);
            log.info("Shift info for '{}' removed from cache", email);
        }
    }
}
