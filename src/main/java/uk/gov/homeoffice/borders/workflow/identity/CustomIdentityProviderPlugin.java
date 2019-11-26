package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import uk.gov.homeoffice.borders.workflow.shift.ShiftApplicationService;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@Order(Ordering.DEFAULT_ORDER + 2)
public class CustomIdentityProviderPlugin extends AbstractCamundaConfiguration {

    private CustomIdentityProviderFactory factory;

    @Override
    public void preInit(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        log.info("Enabling custom identity service....");
        springProcessEngineConfiguration.setIdentityProviderSessionFactory(factory);
        log.info("Custom identity service enabled.");
    }

}
