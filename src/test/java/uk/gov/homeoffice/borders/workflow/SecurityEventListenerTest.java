package uk.gov.homeoffice.borders.workflow;

import org.camunda.bpm.engine.IdentityService;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import uk.gov.homeoffice.borders.workflow.security.SecurityEventListener;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SecurityEventListenerTest {

    private SecurityEventListener underTest;
    private IdentityService identityService;

    @Before
    public void init() {
        identityService = mock(IdentityService.class);
        underTest = new SecurityEventListener(identityService);
    }

    @Test
    public void canReceiveSuccessfulAuthenticationEvent() {
        //given
        KeycloakAuthenticationToken token = mock(KeycloakAuthenticationToken.class);

        //when
        underTest.onSuccessfulAuthentication(new InteractiveAuthenticationSuccessEvent(token, this.getClass()));

        //then
        verify(identityService).setAuthentication(null, Collections.emptyList(), Collections.emptyList());
    }
}
