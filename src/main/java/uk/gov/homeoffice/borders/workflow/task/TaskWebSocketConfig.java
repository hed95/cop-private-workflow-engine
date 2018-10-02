package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.identity.User;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.AbstractHandshakeHandler;
import uk.gov.homeoffice.borders.workflow.security.SecurityConfig;

import java.security.Principal;
import java.util.Map;

import static org.springframework.messaging.simp.SimpMessageType.*;


@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@Profile("!test")
public class TaskWebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {


    @Bean
    public UserTaskEventListener userTaskEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        return new UserTaskEventListener(simpMessagingTemplate);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user/topic");
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SecurityConfig.WEB_SOCKET_TASKS).setAllowedOrigins("*").setHandshakeHandler(new AbstractHandshakeHandler(){
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                final KeycloakAuthenticationToken p = (KeycloakAuthenticationToken) super.determineUser(request, wsHandler, attributes);
                return new KeycloakAuthenticationToken(p.getAccount(), p.isInteractive(), p.getAuthorities()) {
                    private String email() {
                        KeycloakSecurityContext keycloakSecurityContext = ((KeycloakPrincipal) getPrincipal()).getKeycloakSecurityContext();
                        return keycloakSecurityContext.getToken().getEmail();
                    }

                    @Override
                    public String getName() {
                        return email();
                    }
                };
            }
        });
        registry.addEndpoint(SecurityConfig.WEB_SOCKET_TASKS).setAllowedOrigins("*").withSockJS();
    }

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages.simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll()
                .simpDestMatchers("/topic/**", "/user/topic/**").authenticated()
                .simpSubscribeDestMatchers("/topic/**", "/user/topic/**").authenticated()
                .anyMessage().denyAll();

    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
