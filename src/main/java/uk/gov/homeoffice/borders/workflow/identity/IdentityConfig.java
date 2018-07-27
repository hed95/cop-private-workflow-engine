package uk.gov.homeoffice.borders.workflow.identity;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.homeoffice.borders.workflow.PlatformDataUrlBuilder;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class IdentityConfig {

    private RestTemplate platformDataRestTemplate = createRestTemplate();


    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters().stream()
                .filter(m -> !(m instanceof StringHttpMessageConverter))
                .collect(Collectors.toList());
        converters.add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }

    @Autowired
    private PlatformDataUrlBuilder platformDataUrlBuilder;


    @Bean
    public CustomIdentityProviderPlugin identityProviderPlugin() {
        return new CustomIdentityProviderPlugin(customIdentityProviderFactory());
    }

    @Bean
    public UserService userService() {
        return new UserService(platformDataRestTemplate, platformDataUrlBuilder);
    }


    @Bean
    public TeamService teamService() {
        return new TeamService(platformDataRestTemplate, platformDataUrlBuilder);
    }

    @Bean
    public CustomIdentityProviderFactory customIdentityProviderFactory() {
        return new CustomIdentityProviderFactory(customIdentityProvider());
    }

    @Bean
    public CustomIdentityProvider customIdentityProvider() {
        return new CustomIdentityProvider(userService(), teamService());
    }

}
