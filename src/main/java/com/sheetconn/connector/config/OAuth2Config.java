package com.sheetconn.connector.config;

import com.sheetconn.connector.oauth.MultiConnectAuthorizationCodeTokenResponseClient;
import com.sheetconn.connector.oauth.MultiConnectOAuth2AuthorizationRequestResolver;
import com.sheetconn.connector.oauth.OAuth2AuthorizationCodeGrantFilter;
import com.sheetconn.connector.oauth.OAuth2AuthorizationRequestRedirectFilter;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.repository.OAuth2AuthorizeRequestStateRepository;
import com.sheetconn.connector.service.ConnectorRegistryService;
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

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuth2Config {

    private ClientRegistrationConfig clientRegistrationConfig;
    private OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository;

    private ConnectorRegistryService connectorRegistryService;

    private GoogleIdTokenVerifier googleIdTokenVerifier;

    public OAuth2Config(
            ClientRegistrationConfig clientRegistrationConfig,
            OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository,
                        ConnectorRegistryService connectorRegistryService,
            GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.clientRegistrationConfig = clientRegistrationConfig;
        this.authorizeRequestStateRepository = authorizeRequestStateRepository;
        this.connectorRegistryService = connectorRegistryService;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

//    @Bean
//    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService
//        (JdbcOperations jdbcOperations, ClientRegistrationRepository clientRegistrationRepository) {
//        return new JdbcOAuth2AuthorizedClientService(jdbcOperations, clientRegistrationRepository);
//    }

//    @Bean
//	public SecurityFilterChain filterChain(HttpSecurity http,
//                                           ClientRegistrationRepository clientRegistrationRepository,
//                                           OAuth2AuthorizedClientService oAuth2AuthorizedClientService
//    ) throws Exception {
//		http
//			.oauth2Client(oauth2 -> oauth2
//				.clientRegistrationRepository(clientRegistrationRepository)
//				.authorizedClientService(oAuth2AuthorizedClientService)
//				.authorizationCodeGrant(codeGrant -> codeGrant
//					.authorizationRequestRepository(this.authorizationRequestRepository())
//					.authorizationRequestResolver(this.authorizationRequestResolver())
//					.accessTokenResponseClient(this.accessTokenResponseClient())
//				)
//			);
//		return http.build();
//	}
//
//    @Bean
//    public OAuth2AuthorizedClientManager authorizedClientManager(
//            ClientRegistrationRepository clientRegistrationRepository,
//            OAuth2AuthorizedClientService authorizedClientService) {
//
//        OAuth2AuthorizedClientProvider authorizedClientProvider =
//                OAuth2AuthorizedClientProviderBuilder.builder()
//                        .authorizationCode()
//                        .refreshToken()
//                        .build();
//
//        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
//                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
//                        clientRegistrationRepository, authorizedClientService);
//        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
//
//        return authorizedClientManager;
//    }

    @Bean
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    @Bean
    OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        return new MultiConnectOAuth2AuthorizationRequestResolver(this.clientRegistrationRepository(), "/oauth2/authorization", authorizeRequestStateRepository, googleIdTokenVerifier);
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

    @Bean
    FilterRegistrationBean<OncePerRequestFilter> authorizeCodeGrantFilter() {
        FilterRegistrationBean<OncePerRequestFilter> filterBean = new FilterRegistrationBean<>();
        filterBean.setFilter(new OAuth2AuthorizationCodeGrantFilter(
                this.clientRegistrationRepository(),
                connectorRegistryService,
                new OAuth2AuthorizationCodeAuthenticationProvider(this.accessTokenResponseClient()),
                authorizeRequestStateRepository,
                googleIdTokenVerifier
        ));
        filterBean.setOrder(Integer.MAX_VALUE - 10);
        return filterBean;
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
