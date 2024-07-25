package com.example.repo_generator.yaml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "source")
public class SourceLoader {
    private String name;
    private String token;
    private String userId;
    private String userName;
    private String path;
    private String api;
}
