package uk.gov.homeoffice.borders.workflow.config;

import java.util.List;

public interface EngineResourceLoader {

    List<ResourceContainer> getResources();

    String storeType();
}
