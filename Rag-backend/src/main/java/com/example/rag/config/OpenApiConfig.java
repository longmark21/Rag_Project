package com.example.rag.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RAG 知识库 API")
                .version("1.0.0")
                .description("基于 RAG 技术的企业知识库问答系统 API")
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")));
    }
}