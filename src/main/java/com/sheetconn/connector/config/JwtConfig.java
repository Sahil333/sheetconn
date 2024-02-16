package com.sheetconn.connector.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@AllArgsConstructor
public class JwtConfig {

    private Set<String> audience;
}


