package uk.gov.homeoffice.borders.workflow.resource;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
public class AmazonS3EngineResourceLoader implements EngineResourceLoader {

    private AmazonS3 amazonS3;
    private String bucketName;

    @Override
    public List<ResourceContainer> getResources() {
        List<S3ObjectSummary> objectSummaries = amazonS3.listObjects(bucketName).getObjectSummaries();
        if (objectSummaries == null || objectSummaries.isEmpty()) {
            log.info("No resources found within S3 bucket");
            return new ArrayList<>();
        }
        return objectSummaries.stream()
                .map(summary -> {
                    String objectKey = summary.getKey();
                    S3Object object = amazonS3.getObject(bucketName, objectKey);

                    String fileNameMetaData = object.getObjectMetadata().getUserMetadata().get("bpmnFileName");
                    String fileName = fileNameMetaData == null ? objectKey : fileNameMetaData;

                    S3ObjectInputStream objectContent = object.getObjectContent();
                    return new ResourceContainer(new InputStreamResource(objectContent), fileName);
                }).collect(toList());
    }

    @Override
    public String storeType() {
        return "s3";
    }
}
