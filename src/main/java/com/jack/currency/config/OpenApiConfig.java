package com.jack.currency.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI currencyExchangeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Exchange API")
                        .description("Spring Boot REST API for currency exchange rates and conversion")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jack")
                                .url("https://github.com/jack")
                                .email("jack@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/jack/currency-project"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9090")
                                .description("Local development server")
                ));
    }
}