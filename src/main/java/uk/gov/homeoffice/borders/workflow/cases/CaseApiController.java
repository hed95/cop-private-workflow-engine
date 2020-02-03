package uk.gov.homeoffice.borders.workflow.cases;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import static uk.gov.homeoffice.borders.workflow.cases.CasesApiPaths.CASES_ROOT_API;


@RequestMapping(path = CASES_ROOT_API,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApiController {
}
