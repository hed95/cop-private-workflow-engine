package uk.gov.homeoffice.borders.workflow.event;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import uk.gov.homeoffice.borders.workflow.exception.InternalWorkflowException;

import java.io.InputStream;

import static org.apache.commons.io.IOUtils.toInputStream;

@Slf4j
@Component
public class FormToS3PutRequestGenerator {

    public PutObjectRequest request(String form,
                                    ProcessInstance processInstance,
                                    String product) {

        try {
            String businessKey = processInstance.getBusinessKey();
            SpinJsonNode json = Spin.JSON(form);
            String submittedBy = json.jsonPath("$.shiftDetailsContext.email").stringValue();
            String formName = json.jsonPath("$.form.name").stringValue();
            String formVersionId = json.jsonPath("$.form.formVersionId").stringValue();
            String title = json.jsonPath("$.form.title").stringValue();
            String submissionDate = json.jsonPath("$.form.submissionDate").stringValue();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("processinstanceid", processInstance.getId());
            metadata.addUserMetadata("processdefinitionid", processInstance.getProcessDefinitionId());
            metadata.addUserMetadata("formversionid", formVersionId);
            metadata.addUserMetadata("name", formName);
            metadata.addUserMetadata("title", title);
            metadata.addUserMetadata("submittedby", submittedBy);
            metadata.addUserMetadata("submissiondate", submissionDate);


            return new PutObjectRequest(product, this.key(businessKey,
                    formName, submittedBy),
                    null, metadata);
        } catch (Exception e) {
            throw new InternalWorkflowException(e);
        }
    }

    private String key(String businessKey, String formName, String email) {
        StringBuilder keyBuilder = new StringBuilder();
        String timeStamp = DateTime.now().toString("YYYYMMDD'T'HHmmss");

        return keyBuilder.append(businessKey)
                .append("/").append(formName).append("/").append(email).append("-").append(timeStamp).append(".json")
                .toString();

    }

}
