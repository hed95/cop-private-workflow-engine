package uk.gov.homeoffice.borders.workflow.resource;

import java.util.List;

public interface EngineResourceLoader {

    List<ResourceContainer> getResources();

    String storeType();
}
