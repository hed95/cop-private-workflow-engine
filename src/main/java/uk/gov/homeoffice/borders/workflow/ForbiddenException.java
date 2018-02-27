package uk.gov.homeoffice.borders.workflow;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}