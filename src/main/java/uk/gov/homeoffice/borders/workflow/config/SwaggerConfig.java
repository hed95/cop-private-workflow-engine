package uk.gov.homeoffice.borders.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
@EnableSwagger2
@Profile("!prod")
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .consumes(Collections.singleton(MediaType.APPLICATION_JSON_VALUE))
                .produces(Collections.singleton(MediaType.APPLICATION_JSON_VALUE))
                .apiInfo(apiInfo())
                .groupName("uk.gov.homeoffice.borders")
                .select()
                .apis(RequestHandlerSelectors.basePackage("uk.gov.homeoffice.borders.workflow"))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(newArrayList(new ParameterBuilder()
                        .name("Authorization")
                        .description("Access Token. Prefix with Bearer")
                        .modelRef(new ModelRef("string"))
                        .parameterType("header")
                        .required(true)
                        .build()));

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .description("Borders Workflow Engine API")
                .title("Borders Workflow Engine API")
                .build();
    }


}