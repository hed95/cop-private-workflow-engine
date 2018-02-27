package uk.gov.homeoffice.borders.workflow.resource;

import lombok.Value;
import org.springframework.core.io.Resource;

@Value
public class ResourceContainer {

    private Resource resource;
    private String name;
}
