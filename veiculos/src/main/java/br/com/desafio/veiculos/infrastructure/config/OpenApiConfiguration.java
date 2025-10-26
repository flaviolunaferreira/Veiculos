package br.com.desafio.veiculos.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Análise Veicular")
                        .version("v1")
                        .description("API para análise unificada de dados veiculares conforme Desafio Técnico.")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
    
    @Bean
    public OpenApiCustomizer piiRedactionCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> 
            pathItem.readOperations().forEach(operation -> 
                operation.getParameters().stream()
                    .filter(param -> "idveiculo".equals(param.getName()))
                    .forEach(param -> {
                        if (param.getSchema() != null) {
                            param.getSchema().setExtensions(java.util.Map.of("x-pii", true));
                        }
                    })
            )
        );
    }
}

