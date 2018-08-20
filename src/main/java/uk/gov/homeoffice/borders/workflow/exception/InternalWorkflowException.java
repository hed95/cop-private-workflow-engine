package uk.gov.homeoffice.borders.workflow.exception;

public class InternalWorkflowException extends RuntimeException {
    public InternalWorkflowException(Throwable e) {
        super(e);
    }
}
