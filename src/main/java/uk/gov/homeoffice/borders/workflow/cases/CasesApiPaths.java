package uk.gov.homeoffice.borders.workflow.cases;

public class CasesApiPaths {

    private CasesApiPaths() {
    }

    static final String CASES_ROOT_API = "/api/workflow/cases";

    static final String GET_CASE = "/{businessKey}";

    static final String GET_SUBMISSION_DATA = "/{businessKey}/submission";
}


