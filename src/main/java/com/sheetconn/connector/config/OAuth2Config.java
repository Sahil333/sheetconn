package com.sheetconn.connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheetconn.connector.oauth.MultiConnectAuthorizationCodeTokenResponseClient;
import com.sheetconn.connector.oauth.MultiConnectOAuth2AuthorizationRequestResolver;
import com.sheetconn.connector.oauth.OAuth2AuthorizationCodeGrantFilter;
import com.sheetconn.connector.oauth.OAuth2AuthorizationRequestRedirectFilter;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.repository.OAuth2AuthorizeRequestStateRepository;
import com.sheetconn.connector.service.ConnectorRegistryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcOperations;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class OAuth2Config {

    private OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository;
    private ConnectorRegistryService connectorRegistryService;
    private ClientRegistrationRepository clientRegistrationRepository;

    private JwtConfig jwtConfig;

    private ObjectMapper objectMapper;

    public OAuth2Config(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository,
            ConnectorRegistryService connectorRegistryService,
            JwtConfig jwtConfig,
            ObjectMapper objectMapper) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizeRequestStateRepository = authorizeRequestStateRepository;
        this.connectorRegistryService = connectorRegistryService;
        this.jwtConfig = jwtConfig;
        this.objectMapper = objectMapper;
    }

    @Bean
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        return new MultiConnectOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization",
                authorizeRequestStateRepository,
                this.idTokenVerifier());
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new MultiConnectAuthorizationCodeTokenResponseClient();
    }

    @Bean
    FilterRegistrationBean<OncePerRequestFilter> authorizeRequestFilter() {
        FilterRegistrationBean<OncePerRequestFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new OAuth2AuthorizationRequestRedirectFilter(this.authorizationRequestResolver()));
        filterBean.setOrder(Integer.MAX_VALUE - 10);
        return filterBean;
    }

    @Bean("authorizeGrantFilter")
    FilterRegistrationBean<OncePerRequestFilter> authorizeCodeGrantFilter() {
        FilterRegistrationBean<OncePerRequestFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new OAuth2AuthorizationCodeGrantFilter(
                clientRegistrationRepository,
                connectorRegistryService,
                new OAuth2AuthorizationCodeAuthenticationProvider(this.accessTokenResponseClient()),
                authorizeRequestStateRepository,
                this.idTokenVerifier()
        ));
        return filterBean;
    }

    @Bean
    public GoogleIdTokenVerifier idTokenVerifier() {
        return new GoogleIdTokenVerifier(objectMapper, jwtConfig.getAudience());
    }
}
