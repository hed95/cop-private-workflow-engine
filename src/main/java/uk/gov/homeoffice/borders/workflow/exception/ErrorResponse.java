package uk.gov.homeoffice.borders.workflow.exception;


import lombok.Data;

@Data
public class ErrorResponse {
    private int code;
    private String message;
    private Object payload;

}
