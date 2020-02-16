package uk.gov.homeoffice.borders.workflow.cases;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import static uk.gov.homeoffice.borders.workflow.cases.CasesApiPaths.*;


@RequestMapping(path = CASES_ROOT_API,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
public class CaseApiController {

    private CasesApplicationService casesApplicationService;
    private PagedResourcesAssembler<Case> pagedResourcesAssembler;

    @GetMapping
    public PagedResources<Case> getCases(Pageable pageable,
                                         @RequestParam("businessKeyQuery") String businessKeyQuery,
                                         PlatformUser platformUser) {

        Page<Case> cases = casesApplicationService.queryByKey(businessKeyQuery, pageable, platformUser);
        return pagedResourcesAssembler.toResource(cases, entity -> entity);
    }

    @GetMapping(path = GET_CASE)
    public ResponseEntity<CaseDetail> getCaseDetails(@PathVariable String businessKey,
                                                     PlatformUser platformUser) {
        CaseDetail caseDetail = casesApplicationService.getByKey(businessKey, platformUser);
        return ResponseEntity.ok(caseDetail);
    }

    @GetMapping(path = GET_SUBMISSION_DATA)
    public ResponseEntity<Object> getSubmissionData(@PathVariable String businessKey,
                                                    @RequestParam String key,
                                                    PlatformUser platformUser) {
        SpinJsonNode submissionData = casesApplicationService.getSubmissionData(businessKey,
                key, platformUser);

        return ResponseEntity.ok(submissionData.toString());
    }


}
