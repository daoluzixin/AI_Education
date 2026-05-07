package org.example.ai_educatin.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("博文学堂 API")
                        .description("博文学堂 - 家长大学生家教O2O撮合平台接口文档")
                        .version("1.0.0"));
    }
}
