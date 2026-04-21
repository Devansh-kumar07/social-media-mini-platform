package com.connectsphere.social.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI connectsphereServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectSphere Post API")
                        .version("v1")
                        .description("Basic swagger setup for post service endpoints.")
                        .contact(new Contact().name("ConnectSphere Team")))
                .components(new Components()
                        .addSecuritySchemes("x-user-id", new SecurityScheme()
                                .name("X-User-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)));
    }
}
