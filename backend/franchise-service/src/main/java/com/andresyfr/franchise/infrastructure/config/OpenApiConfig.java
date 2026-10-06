package com.andresyfr.franchise.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public OpenAPI franchiseOpenApi(@Value("${info.application.version:1.0.0}") String version) {
        return new OpenAPI().info(new Info()
                .title("Franchise API")
                .description("Reactive API for managing franchises, branches and product stock")
                .version(version)
                .contact(new io.swagger.v3.oas.models.info.Contact()
                        .name("Andresyfr")
                        .email("github.com/andresyfr")
                        .url("https://www.github.com/andresyfr")
                )
                .license(new License().name("AGPL-3.0").url("https://choosealicense.com/es/licenses/agpl-3.0/")));
    }
}
