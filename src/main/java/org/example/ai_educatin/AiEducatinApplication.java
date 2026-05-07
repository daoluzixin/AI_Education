package org.example.ai_educatin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.ai_educatin.mapper")
public class AiEducatinApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEducatinApplication.class, args);
    }

}
