package uk.gov.homeoffice.borders.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(value = ResourceNotFound.class)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFound e) {
        log.error("Resource not found exception", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(NOT_FOUND.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPayload(e.getMessage());
        return errorResponse;

    }

    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler(value = ForbiddenException.class)
    public ErrorResponse handleForbiddenException(ForbiddenException e) {
        log.error("Unauthorised exception", e);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(UNAUTHORIZED.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPayload(e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handIllegalArgumentException(IllegalArgumentException e){
        log.error("Bad request error", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(BAD_REQUEST.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPayload(e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e){
        log.error("Internal server error", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPayload(e.getMessage());
        return errorResponse;
    }
}
