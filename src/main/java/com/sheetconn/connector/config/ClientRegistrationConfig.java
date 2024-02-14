package com.sheetconn.connector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
@Getter
@Setter
public class ClientRegistrationConfig {

    Map<String, Map<String, String>> registration;
    Map<String, Map<String, String>> provider;

}
