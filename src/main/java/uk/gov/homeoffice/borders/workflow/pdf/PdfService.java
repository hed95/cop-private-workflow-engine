package uk.gov.homeoffice.borders.workflow.pdf;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.event.FormToS3Uploader;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
public class PdfService {

    private final AmazonS3 amazonS3;
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final RuntimeService runtimeService;
    private final Environment environment;
    private final RestTemplate restTemplate;

    public PdfService(AmazonS3 amazonS3, AmazonSimpleEmailService amazonSimpleEmailService,
                      RuntimeService runtimeService, Environment environment, RestTemplate restTemplate) {
        this.amazonS3 = amazonS3;
        this.amazonSimpleEmailService = amazonSimpleEmailService;
        this.runtimeService = runtimeService;
        this.environment = environment;
        this.restTemplate = restTemplate;
    }


    public void requestPdfGeneration(@NotNull SpinJsonNode form,
                                     @NotNull SpinJsonNode formData,
                                     @NotNull String businessKey,
                                     @NotNull String processInstanceId,
                                     String message) {
        requestPdfGeneration(
                form,
                businessKey,
                null,
                processInstanceId,
                message,
                formData
        );

    }

    public void requestPdfGeneration(@NotNull SpinJsonNode form,
                                     @NotNull String businessKey,
                                     @NotNull String processInstanceId) {
        requestPdfGeneration(form, businessKey, null, processInstanceId);
    }


    public void requestPdfGeneration(@NotNull SpinJsonNode form,
                                     @NotNull String businessKey,
                                     String product,
                                     @NotNull String processInstanceId) {
        requestPdfGeneration(form, businessKey, product, processInstanceId, null, null);
    }

    public void requestPdfGeneration(@NotNull SpinJsonNode form,
                                     @NotNull String businessKey,
                                     String product,
                                     @NotNull String processInstanceId,
                                     String callbackMessage,
                                     SpinJsonNode formData) {

        JSONObject formAsJson = new JSONObject(form.toString());

        String productPrefix = environment.getProperty("aws.bucket-name-prefix");
        String bucket = productPrefix + "-" + Optional.ofNullable(product).orElse("cop-case");
        String formApiUrl = environment.getProperty("form-api.url");
        String formName = formAsJson.getString("name");

        String message = Optional.ofNullable(callbackMessage)
                .orElse(format("pdfGenerated_%s_%s", formName, formAsJson.getString("submissionDate")));
        String key = generateKey(formAsJson, businessKey);
        JSONObject payload = new JSONObject();

        try {
            if (formData == null) {
                S3Object object = amazonS3.getObject(bucket, key);
                String asJsonString = IOUtils.toString(object.getObjectContent(),
                        StandardCharsets.UTF_8);
                payload.put("submission", new JSONObject().put("data", asJsonString).toString());
            } else {
                payload.put("submission",  new JSONObject().put("data", formData.toString()).toString());
            }


            payload.put("webhookUrl", format("%s/v1/api/workflow/web-hook/processInstance/%s/message/%s?variableName=%s",
                    environment.getProperty("engine.webhook.url"), processInstanceId, message,
                    formName));
            payload.put("formUrl", format("%s/form/version/%s", formApiUrl, formAsJson.getString("versionId")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> body = new HttpEntity<>(payload.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    format("%s/pdf", formApiUrl),
                    HttpMethod.POST,
                    body,
                    String.class
            );

            log.info("PDF request submitted response status '{}'", response.getStatusCodeValue());
        } catch (Exception e) {
            try {
                runtimeService.createIncident(
                        "FAILED_TO_REQUEST_PDF_GENERATION",
                        processInstanceId,
                        new JSONObject(Map.of(
                                "formName", formName,
                                "dataKey", key,
                                "bucketName", bucket,
                                "exception", e.getMessage()

                        )).toString()
                );
            } catch (Exception rex) {
                log.error("Failed to create incident {}", rex.getMessage());
            }
            throw new BpmnError("failedToGeneratePDF", e.getMessage());
        }

    }

    private String generateKey(JSONObject formAsJson, String businessKey) {
        String submittedBy = formAsJson.getString("submittedBy");
        String submissionDate = formAsJson.getString("submissionDate");
        String formName = formAsJson.getString("name");
        return FormToS3Uploader.key(businessKey, formName, submittedBy, submissionDate);
    }
}
