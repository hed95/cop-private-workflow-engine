package uk.gov.homeoffice.borders.workflow.security;

import org.apache.commons.lang3.ArrayUtils;
import org.camunda.bpm.engine.IdentityService;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.util.TagUtils.SCOPE_REQUEST;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@Profile("!test")
@EnableRedisHttpSession
@EnableScheduling
@SuppressWarnings("unchecked")
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {


    private static final String ENGINE = "/engine";
    private static final String ACTUATOR_HEALTH = "/actuator/health";
    private static final String ACTUATOR_METRICS = "/actuator/metrics";
    public static final String WEB_SOCKET_TASKS = "/ws/workflow/tasks";
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    static final List<String> NO_AUTH_URLS = List.of(ArrayUtils.addAll(SWAGGER_WHITELIST, ENGINE, ACTUATOR_HEALTH, ACTUATOR_METRICS, WEB_SOCKET_TASKS));

    public final KeycloakClientRequestFactory keycloakClientRequestFactory;

    public final RedisConnectionFactory redisConnectionFactory;


    public SecurityConfig(KeycloakClientRequestFactory keycloakClientRequestFactory, RedisConnectionFactory redisConnectionFactory) {
        this.keycloakClientRequestFactory = keycloakClientRequestFactory;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        SimpleAuthorityMapper grantedAuthoritiesMapper = new SimpleAuthorityMapper();
        grantedAuthoritiesMapper.setPrefix("");
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(grantedAuthoritiesMapper);
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Scope(scopeName = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
    public KeycloakSecurityContext securityContext() {
        return (KeycloakSecurityContext)
                RequestContextHolder.currentRequestAttributes()
                        .getAttribute(KeycloakSecurityContext.class.getName(), RequestAttributes.SCOPE_REQUEST);

    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(
                springSessionBackedRegistry());
    }

    @Bean
    @SuppressWarnings("unchecked")
    public SessionRegistry springSessionBackedRegistry() {
        return new SpringSessionBackedSessionRegistry<Session>(sessionRepository());
    }

    @Bean
    @SuppressWarnings("unchecked")
    public FindByIndexNameSessionRepository sessionRepository() {
        return new RedisIndexedSessionRepository((RedisTemplate) redisTemplate());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(WEB_SOCKET_TASKS).permitAll()
                .antMatchers(SWAGGER_WHITELIST).permitAll()
                .antMatchers(ENGINE).permitAll()
                .antMatchers(ACTUATOR_HEALTH).permitAll()
                .antMatchers(ACTUATOR_METRICS).permitAll()
                .anyRequest()
                .fullyAuthenticated();
    }


    @Bean
    @Order()
    public ProcessEngineIdentityFilter processEngineFilter(IdentityService identityService,
                                                           KeycloakSecurityContext securityContext) {
        return new ProcessEngineIdentityFilter(identityService, securityContext, new AntPathMatcher());
    }


}
