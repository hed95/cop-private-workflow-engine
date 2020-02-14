package uk.gov.homeoffice.borders.workflow.cases;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import java.util.Date;


@Getter
public class CaseAudit extends ApplicationEvent {

    private Object[] args;
    private PlatformUser platformUser;
    private Date date;
    private String type;

    public CaseAudit(Object source,
                     Object[] args,
                     PlatformUser platformUser,
                     String type) {
        super(source);
        this.type = type;
        this.args = args;
        this.platformUser = platformUser;
        this.date = new Date();
    }
}
