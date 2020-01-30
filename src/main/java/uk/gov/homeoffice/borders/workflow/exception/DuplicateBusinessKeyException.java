package uk.gov.homeoffice.borders.workflow.exception;

import lombok.Getter;

@Getter
public class DuplicateBusinessKeyException extends RuntimeException {

    private String businessKey;

    public DuplicateBusinessKeyException(String message, String businessKey) {
        super(message);
        this.businessKey = businessKey;
    }
}
