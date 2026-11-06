package com.deliverytech.delivery_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                .info(new Info()
                        .title("DeliveryTech API")
                        .version("0.0.10")
                        .description("API REST completa para plataforma de delivery")
                        .contact(new Contact()
                                .name("Equipe DeliveryTech")
                                .email("dev@deliverytech.com")
                                .url("https://deliverytech.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )

                // Configuração do JWT Bearer
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Informe seu token JWT")
                        )
                )

                // Aplica autenticação às operações documentadas
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

                // Servidores disponíveis
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),

                        new Server()
                                .url("https://api.deliverytech.com")
                                .description("Servidor de Produção")
                ));
    }
}
