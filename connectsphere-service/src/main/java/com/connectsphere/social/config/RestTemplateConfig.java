package com.connectsphere.social.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class creates a RestTemplate bean.
 * RestTemplate is Spring's simple HTTP client — it lets us call other services over HTTP.
 * Think of it like a remote-control to talk to other microservices.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
