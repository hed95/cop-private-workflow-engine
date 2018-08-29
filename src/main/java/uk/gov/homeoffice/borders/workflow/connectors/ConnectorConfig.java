package uk.gov.homeoffice.borders.workflow.connectors;

import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorConfig {

    public ConnectProcessEnginePlugin connectProcessEnginePlugin() {
        return new ConnectProcessEnginePlugin();
    }
}
