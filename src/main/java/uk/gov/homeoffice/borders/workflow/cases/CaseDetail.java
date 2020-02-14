package uk.gov.homeoffice.borders.workflow.cases;


import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CaseDetail {

    private String businessKey;
    private List<ProcessInstanceReference> processInstances;

    @Data
    public static class ProcessInstanceReference {
        private String id;
        private String key;
        private String name;
        private String definitionId;
        private List<FormReference> formReferences;
        private Date startDate;
        private Date endDate;
    }

    @Data
    public static class FormReference {
        private String name;
        private String title;
        private String versionId;
        private String dataPath;
        private String submissionDate;
        private String submittedBy;
    }
}
