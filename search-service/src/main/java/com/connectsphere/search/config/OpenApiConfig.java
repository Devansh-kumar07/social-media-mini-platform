package com.connectsphere.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI searchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Search Service API")
                        .version("v1")
                        .description("Simple search and hashtag endpoints.")
                        .contact(new Contact().name("ConnectSphere Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ));
    }
}
