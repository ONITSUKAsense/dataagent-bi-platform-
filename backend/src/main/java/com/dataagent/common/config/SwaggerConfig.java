package com.dataagent.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DataAgent API")
                        .version("1.0.0")
                        .description("DataAgent 智能 BI 报告平台接口文档")
                        .contact(new Contact().name("DataAgent Team"))
                        .license(new License().name("Apache 2.0")));
    }
}
