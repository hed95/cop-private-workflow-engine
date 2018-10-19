package uk.gov.homeoffice.borders.workflow.task;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.session.Session;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.support.AbstractHandshakeHandler;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;
import uk.gov.homeoffice.borders.workflow.security.SecurityConfig;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.messaging.simp.SimpMessageType.*;


@Configuration
@EnableWebSocketMessageBroker
@Slf4j
@Profile("!test")
public class TaskWebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session> {

    @Bean
    public UserTaskEventListener userTaskEventListener(SimpMessagingTemplate simpMessagingTemplate,
                                                       PlatformDataUrlBuilder platformDataUrlBuilder, RestTemplate restTemplate) {
        return new UserTaskEventListener(simpMessagingTemplate, platformDataUrlBuilder, restTemplate);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        long heartbeatServer = 5000;
        long heartbeatClient = 5000;

        ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
        te.setPoolSize(2);
        te.setThreadNamePrefix("wss-heartbeat-thread-");
        te.initialize();

        config.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(te)
                .setHeartbeatValue(new long[]{heartbeatServer, heartbeatClient});
    }


    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SecurityConfig.WEB_SOCKET_TASKS).setAllowedOrigins("*")
                .setHandshakeHandler(new AbstractHandshakeHandler() {
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
        }).withSockJS()
                .setHeartbeatTime(TimeUnit.SECONDS.toMillis(10))
                .setDisconnectDelay(TimeUnit.SECONDS.toMillis(120));

    }


    @Configuration
    @Profile("!test")
    public static class TaskWeSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
        @Override
        protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
            messages.simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT, HEARTBEAT).permitAll()
                    .simpDestMatchers("/topic/**", "/queue/**", "/user/queue/**").authenticated()
                    .simpSubscribeDestMatchers("/topic/**", "/queue/**", "/user/queue/**").authenticated()
                    .anyMessage().denyAll();

        }

        @Override
        protected boolean sameOriginDisabled() {
            return true;
        }

    }
}