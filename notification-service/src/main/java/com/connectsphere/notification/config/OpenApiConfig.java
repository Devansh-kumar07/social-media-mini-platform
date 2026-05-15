package com.connectsphere.notification.config;

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
    OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("v1")
                        .description("Simple notification inbox endpoints.")
                        .contact(new Contact().name("ConnectSphere Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ));
    }
}
