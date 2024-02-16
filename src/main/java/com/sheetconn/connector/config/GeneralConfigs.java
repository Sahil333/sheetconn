package com.sheetconn.connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class GeneralConfigs {

    private final ClientRegistrationConfig clientRegistrationConfig;

    public GeneralConfigs(ClientRegistrationConfig clientRegistrationConfig) {
        this.clientRegistrationConfig = clientRegistrationConfig;
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        Map<String, Map<String, String>> registration = clientRegistrationConfig.getRegistration();
        Map<String, Map<String, String>> provider = clientRegistrationConfig.getProvider();
        List<ClientRegistration> clientRegistrations = new ArrayList<>();
        for(Map.Entry<String, Map<String, String>> registry: registration.entrySet()) {
            ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registry.getKey())
                    .clientId(registry.getValue().get("client-id"))
                    .clientSecret(registry.getValue().get("client-secret"))
                    .authorizationGrantType(new AuthorizationGrantType(registry.getValue().get("authorization-grant-type")))
                    .redirectUri(registry.getValue().get("redirect-uri"))
                    .scope(registry.getValue().get("scope").split(","))
                    .authorizationUri(provider.get(registry.getKey()).get("authorization-uri"))
                    .tokenUri(provider.get(registry.getKey()).get("token-uri"))
                    .build();
            clientRegistrations.add(clientRegistration);
        }

        return new InMemoryClientRegistrationRepository(clientRegistrations);
    }
}
