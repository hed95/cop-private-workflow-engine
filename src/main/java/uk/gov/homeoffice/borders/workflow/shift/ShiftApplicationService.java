package uk.gov.homeoffice.borders.workflow.shift;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.ResourceNotFound;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Service responsible for dealing with the internal
 * workflow for creating an active shift
 */

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ShiftApplicationService {

    private RuntimeService runtimeService;

    private RestTemplate restTemplate;

    private PlatformDataUrlBuilder platformDataUrlBuilder;

    private String platformDataToken;

    /**
     * Start a shift workflow
     *
     * @param shiftInfo Contains information about a shift
     * @return processInstance created
     * @see ShiftInfo
     * @see ProcessInstance
     */
    public ProcessInstance startShift(@NotNull ShiftInfo shiftInfo) {

        String email = shiftInfo.getEmail();
        log.info("Starting a request to start a shift for '{}'", email);

        ProcessInstance shiftForPerson = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(email)
                .singleResult();

        if (shiftForPerson != null) {
            deleteShift(email);
            log.info("Removed previous shift workflow for '{}'", email);
        }

        setEndTime(shiftInfo);

        ObjectValue shiftVariableObject =
                Variables.objectValue(shiftInfo)
                        .serializationDataFormat(MediaType.APPLICATION_JSON_VALUE)
                        .create();

        Map<String, Object> variables = new HashMap<>();
        variables.put("shiftInfo", shiftVariableObject);
        variables.put("type", "non-notification");

        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("activate-shift", email, variables);
        log.info("Shift process for '{}' has started '{}'", email, processInstance.getProcessInstanceId());
        return processInstance;
    }

    private void setEndTime(ShiftInfo shiftInfo) {
        Integer shiftHours = shiftInfo.getShiftHours();
        Integer shiftMinutes = shiftInfo.getShiftMinutes();
        Date startTime = shiftInfo.getStartDateTime();
        shiftInfo.setEndDateTime(new DateTime(startTime)
                .plusHours(shiftHours)
                .plusMinutes(shiftMinutes)
                .toDate());
    }

    /**
     * Deletes a workflow with the given email
     *
     * @param email        identifies the shift that needs to be deleted
     * @param deleteReason This is required and explains why the workflow was cancelled.
     * @see ProcessInstance
     */
    public void deleteShift(@NotNull String email, @NotNull String deleteReason) {
        ProcessInstance shiftForStaff = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(email)
                .singleResult();

        if (shiftForStaff == null) {
            log.warn("Process instance does not exist based on email '{}'", email);
            throw new ResourceNotFound("Shift details does not exist for '" + email + "'");
        }
        deleteShift(email);
        log.info("active shift deleted from store...deleting workflow");
        runtimeService.deleteProcessInstance(shiftForStaff.getProcessInstanceId(), deleteReason);
        log.info("Shift deleted for '{}'", email);
    }

    private void deleteShift(@NotNull String email) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + platformDataToken);
        restTemplate.exchange(platformDataUrlBuilder.shiftUrl(email),
                HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
        log.info("active shift deleted from store..");

    }

    /**
     * Get shift info for given email
     *
     * @param email
     * @return shiftInfo.
     * @throws ResourceNotFound shift info cannot be found
     * @see ShiftInfo
     */
    public ShiftInfo getShiftInfo(@NotNull String email) {
        ProcessInstance shift = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(email)
                .singleResult();

        if (shift == null) {
            throw new ResourceNotFound("Shift detail not found for '" + email + "'");
        }

        VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(shift.getProcessInstanceId())
                .variableName("shiftInfo").singleResult();

        if (variableInstance != null) {
            return (ShiftInfo) variableInstance.getValue();
        }

        throw new ResourceNotFound("Shift data could not be found");
    }

}
