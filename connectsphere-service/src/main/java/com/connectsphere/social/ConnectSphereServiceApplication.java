package com.connectsphere.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ConnectSphereServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConnectSphereServiceApplication.class, args);
    }
}

