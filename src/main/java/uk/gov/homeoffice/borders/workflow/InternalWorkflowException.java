package uk.gov.homeoffice.borders.workflow;

public class InternalWorkflowException extends RuntimeException {
    public InternalWorkflowException(Throwable e) {
        super(e);
    }
}
