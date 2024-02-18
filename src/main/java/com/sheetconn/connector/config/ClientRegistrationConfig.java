package com.sheetconn.connector.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@AllArgsConstructor
public class ClientRegistrationConfig {

    Map<String, Map<String, String>> registration;
    Map<String, Map<String, String>> provider;

}
