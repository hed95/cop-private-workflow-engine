package uk.gov.homeoffice.borders.workflow.task;

public class NotifyFailureException extends RuntimeException {

    public NotifyFailureException(Throwable throwable) {
        super(throwable);
    }
}
