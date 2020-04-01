package uk.gov.homeoffice.borders.workflow.cases;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceDto;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import uk.gov.homeoffice.borders.workflow.PageHelper;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CasesApplicationService {

    private HistoryService historyService;
    private AmazonS3 amazonS3Client;
    private RestHighLevelClient elasticsearchClient;
    private AWSConfig awsConfig;
    private CaseActionService caseActionService;
    private AuthorizationService authorizationService;
    private static final PageHelper PAGE_HELPER = new PageHelper();

    /**
     * Query for cases that match a key. Each case is a collection of process instance pointers. No internal data
     * is returned.
     *
     * @param query
     * @param pageable
     * @param platformUser
     * @return a list of cases.
     */
    @AuditableCaseEvent
    public Page<Case> queryByKey(String query, Pageable pageable, PlatformUser platformUser) {
        log.info("Performing search by {}", platformUser.getEmail());

        final SearchRequest searchRequest = new SearchRequest();

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.simpleQueryStringQuery(query));
        sourceBuilder.from(pageable.getPageNumber());
        sourceBuilder.size(pageable.getPageSize());
        sourceBuilder.fetchSource(new String[]{"businessKey"}, null);
        searchRequest.source(sourceBuilder);

        try {
            final SearchResponse results = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

            final List<String> keys = StreamSupport.stream(results.getHits().spliterator(), false)
                    .map(r -> r.getField("businessKey").getValue().toString()).collect(toList());

            List<HistoricProcessInstance> historicProcessInstances = new ArrayList<>();
            if (!keys.isEmpty()) {
                final List<HistoricProcessInstance> instances = keys.stream()
                        .map(key -> historyService.createHistoricProcessInstanceQuery()
                                .processInstanceBusinessKey(key).list()).flatMap(List::stream).collect(toList());
                historicProcessInstances.addAll(instances);
            }
            Map<String, List<HistoricProcessInstance>> groupedByBusinessKey = historicProcessInstances
                    .stream().collect(Collectors.groupingBy(HistoricProcessInstance::getBusinessKey));
            List<Case> cases = groupedByBusinessKey.keySet().stream().map(key -> {
                Case caseDto = new Case();
                caseDto.setBusinessKey(key);
                List<HistoricProcessInstance> instances = groupedByBusinessKey.get(key);
                caseDto.setProcessInstances(instances
                        .stream()
                        .map(HistoricProcessInstanceDto::fromHistoricProcessInstance).collect(toList()));
                return caseDto;
            }).collect(toList());

            final long totalHits = results.getHits().getTotalHits();
            log.info("Number of cases returned for '{}' is '{}'", query, totalHits);
            return new PageImpl<>(cases, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), totalHits);

        } catch (Exception e) {
            log.error("Failed to perform search '{}'", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                    0);
        }
    }


    @AuditableCaseEvent
    @PostAuthorize(value = "@caseAuthorizationEvaluator.isAuthorized(returnObject, #platformUser)")
    public CaseDetail getByKey(String businessKey, PlatformUser platformUser) {

        log.info("Beginning case detail fetch");
        CaseDetail caseDetail = new CaseDetail();
        caseDetail.setBusinessKey(businessKey);


        ObjectListing objectListing = amazonS3Client
                .listObjects(awsConfig.getCaseBucketName(), format("%s/", businessKey));


        List<ObjectMetadata> metadata = new ArrayList<>();
        objectListing.getObjectSummaries().forEach(summary -> {
            ObjectMetadata objectMetadata = amazonS3Client
                    .getObjectMetadata(new GetObjectMetadataRequest(summary.getBucketName(), summary.getKey()));
            objectMetadata.addUserMetadata("key", summary.getKey());
            metadata.add(objectMetadata);

        });


        Map<String, List<ObjectMetadata>> byProcessInstanceId = metadata
                .stream()
                .collect(Collectors.groupingBy(meta -> meta.getUserMetaDataOf("processinstanceid")));


        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey).list();

        List<CaseDetail.ProcessInstanceReference> instanceReferences = processInstances
                .stream()
                .filter(instance -> this.candidateGroupFilter(instance, platformUser))
                .map(historicProcessInstance ->
                        toCaseReference(byProcessInstanceId, historicProcessInstance))
                .collect(toList());

        caseDetail.setProcessInstances(instanceReferences);

        try {
            log.info("Adding actions to case details");
            caseDetail.setActions(caseActionService.getAvailableActions(caseDetail, platformUser));
            log.info("No of actions for case '{}'", caseDetail.getActions().size());
        } catch (Exception e) {
            log.error("Failed to build actions", e);
        }

        try {
            CaseDetail.CaseMetrics metrics = createMetrics(caseDetail);
            caseDetail.setMetrics(metrics);
        } catch (Exception e) {
            log.error("Failed to build metrics", e);
        }

        log.info("Returning case details to '{}' with business key '{}'", platformUser.getEmail(), businessKey);
        return caseDetail;
    }

    /**
     * Applies a filter. If there are no identity links for a process instance then the instance is returned.
     * If there are identity links then the users roles are compared against the links. If any match then
     * the instance is returned otherwise it is not.
     *
     * @param historicProcessInstance
     * @param platformUser
     * @return true/false
     */
    private boolean candidateGroupFilter(HistoricProcessInstance historicProcessInstance, PlatformUser platformUser) {

        List<Authorization> authorizations = authorizationService.createAuthorizationQuery()
                .resourceId(historicProcessInstance.getId())
                .resourceType(Resources.PROCESS_INSTANCE)
                .list();

        if (authorizations.isEmpty()) {
            return true;
        }

        List<String> candidateUsers =
                authorizations.stream().map(Authorization::getUserId).filter(Objects::nonNull)
                        .collect(Collectors.toList());

        if (!candidateUsers.isEmpty()) {
            return candidateUsers.contains(platformUser.getEmail());
        }
        List<String> candidateGroups =
                authorizations.stream().map(Authorization::getGroupId).filter(Objects::nonNull)
                        .collect(Collectors.toList());


        List<String> roles = platformUser.getShiftDetails().getRoles();
        return !roles.stream().filter(candidateGroups::contains).collect(Collectors.toList()).isEmpty();

    }

    private CaseDetail.CaseMetrics createMetrics(CaseDetail caseDetail) {

        CaseDetail.CaseMetrics metrics = new CaseDetail.CaseMetrics();

        List<CaseDetail.ProcessInstanceReference> completedProcessInstances = caseDetail.getProcessInstances().stream()
                .filter(p -> p.getEndDate() != null).collect(toList());

        metrics.setNoOfCompletedProcessInstances((long) completedProcessInstances.size());

        metrics.setNoOfRunningProcessInstances(caseDetail.getProcessInstances().stream()
                .filter(p -> p.getEndDate() == null).count());


        List<String> processInstanceIds = caseDetail.getProcessInstances()
                .stream()
                .map(CaseDetail.ProcessInstanceReference::getId).collect(toList());

        Long totalOpenUserTasks = processInstanceIds
                .stream().map(id -> historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(id)
                        .activityType(ActivityTypes.TASK_USER_TASK)
                        .unfinished()
                        .count()).mapToLong(Long::longValue).sum();

        metrics.setNoOfOpenUserTasks(totalOpenUserTasks);

        Long totalCompletedUserTasks = processInstanceIds
                .stream().map(id -> historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(id)
                        .activityType(ActivityTypes.TASK_USER_TASK)
                        .finished()
                        .count()).mapToLong(Long::longValue).sum();

        metrics.setNoOfCompletedUserTasks(totalCompletedUserTasks);


        long overallTimeInSeconds = completedProcessInstances.stream()
                .map(p -> {
                    long difference = Duration.between(p.getStartDate().toInstant(),
                            p.getEndDate().toInstant()).toSeconds();
                    return Math.abs(difference);
                })
                .mapToLong(Long::longValue).sum();

        metrics.setOverallTimeInSeconds(overallTimeInSeconds);

        if (overallTimeInSeconds != 0) {
            long averageTimeForCompletedInstances = overallTimeInSeconds /
                    metrics.getNoOfCompletedProcessInstances();
            metrics.setAverageTimeToCompleteProcessInSeconds(averageTimeForCompletedInstances);
        } else {
            metrics.setAverageTimeToCompleteProcessInSeconds(0L);
        }

        return metrics;
    }


    private CaseDetail.ProcessInstanceReference toCaseReference(Map<String, List<ObjectMetadata>> byProcessInstanceId,
                                                                HistoricProcessInstance historicProcessInstance) {
        CaseDetail.ProcessInstanceReference reference = new CaseDetail.ProcessInstanceReference();
        reference.setId(historicProcessInstance.getId());
        reference.setDefinitionId(historicProcessInstance.getProcessDefinitionId());
        reference.setName(historicProcessInstance.getProcessDefinitionName());
        reference.setKey(historicProcessInstance.getProcessDefinitionKey());
        reference.setStartDate(historicProcessInstance.getStartTime());
        reference.setEndDate(historicProcessInstance.getEndTime());

        List<ObjectMetadata> metadataByProcessDefinition = byProcessInstanceId.get(historicProcessInstance.getId());
        if (metadataByProcessDefinition != null) {
            reference.setFormReferences(
                    metadataByProcessDefinition.stream().map(this::toFormReference).collect(toList())
            );
        }
        return reference;
    }


    private CaseDetail.FormReference toFormReference(final ObjectMetadata metadata) {
        final CaseDetail.FormReference formReference = new CaseDetail.FormReference();
        formReference.setVersionId(metadata.getUserMetaDataOf("formversionid"));
        formReference.setName(metadata.getUserMetaDataOf("name"));
        formReference.setTitle(metadata.getUserMetaDataOf("title"));
        formReference.setDataPath(metadata.getUserMetaDataOf("key"));
        formReference.setSubmissionDate(metadata.getUserMetaDataOf("submissiondate"));
        Optional.ofNullable(metadata.getUserMetaDataOf("submittedby"))
                .ifPresent(user -> formReference.setSubmittedBy(
                        URLDecoder.decode(metadata.getUserMetaDataOf("submittedby"),
                                Charset.forName("UTF-8"))));

        return formReference;

    }


    @AuditableCaseEvent
    @PostAuthorize(value = "@caseAuthorizationEvaluator.isAuthorized(returnObject, #platformUser)")
    public SpinJsonNode getSubmissionData(String businessKey, String submissionDataKey, PlatformUser platformUser) {
        S3Object object = amazonS3Client.getObject(awsConfig.getCaseBucketName(), submissionDataKey);
        try {
            String asJsonString = IOUtils.toString(object.getObjectContent(), "UTF-8");
            return Spin.JSON(asJsonString);
        } catch (IOException e) {
            throw new InternalWorkflowException(e);
        }

    }


}
