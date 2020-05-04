package uk.gov.homeoffice.borders.workflow.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.task.notifications.Notification;

import static uk.gov.homeoffice.borders.workflow.exception.ErrorCodes.GOV_NOTIFY_ERROR_CODE;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExceptionHandler {

    private ObjectMapper objectMapper;

    public void registerNotification(Exception exception, Notification notification) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(GOV_NOTIFY_ERROR_CODE);
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setPayload(notification);
        try {
            log.error(objectMapper.writeValueAsString(errorResponse));
        } catch (JsonProcessingException e) {
            throw new InternalWorkflowException(e);
        }
    }


}
