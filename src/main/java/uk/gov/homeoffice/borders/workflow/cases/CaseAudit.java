package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import java.util.Date;


@Getter
public class CaseAudit extends ApplicationEvent {

    private PlatformUser requestBy;
    private String businessKey;
    private Date date;
    private String type;

    public CaseAudit(Object source,
                     String businessKey,
                     String type,
                     PlatformUser requestBy) {
        super(source);
        this.type = type;
        this.requestBy = requestBy;
        this.businessKey = businessKey;
        this.date = new Date();
    }
}
