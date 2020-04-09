package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(path = "/admin/reindex/case")
@Slf4j
public class CaseReIndexController {


    private final CaseReIndexer caseReindexer;

    public CaseReIndexController(CaseReIndexer caseReindexer) {
        this.caseReindexer = caseReindexer;
    }

    @PostMapping(path = "/caseId")
    @PreAuthorize("@caseReIndexAuthorizationChecker.isAuthorized(authentication)")
    public ResponseEntity<?> reindex(@PathVariable String caseId) {
        caseReindexer.reindex(caseId, CaseReIndexer.DEFAULT_LISTENER);
        log.info("Request to index accepted");
        return ResponseEntity.accepted().build();
    }
}
