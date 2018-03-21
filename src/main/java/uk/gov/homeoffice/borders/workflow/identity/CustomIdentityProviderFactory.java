package uk.gov.homeoffice.borders.workflow.identity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomIdentityProviderFactory implements SessionFactory {

    private CustomIdentityProvider customIdentityProvider;

    @Override
    public Class<?> getSessionType() {
        return ReadOnlyIdentityProvider.class;
    }

    @Override
    public Session openSession() {
        return customIdentityProvider;
    }
}
