package com.project.taskmanagement;

import com.project.taskmanagement.config.CorsProperties;
import com.project.taskmanagement.config.FileSecurityProperties;
import com.project.taskmanagement.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        CorsProperties.class,
        FileSecurityProperties.class,
        JwtProperties.class
})
public class BaseV1Application {

    public static void main(String[] args) {
        SpringApplication.run(BaseV1Application.class, args);
    }

}
