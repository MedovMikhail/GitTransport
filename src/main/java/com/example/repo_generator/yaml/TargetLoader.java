package com.example.repo_generator.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "target")
public class TargetLoader {
    private String name;
    private String token;
    private String path;
    private String userName;
    private String api;
}
