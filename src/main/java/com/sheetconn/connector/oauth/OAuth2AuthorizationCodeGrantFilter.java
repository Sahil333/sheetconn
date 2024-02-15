package com.sheetconn.connector.oauth;

import com.sheetconn.connector.model.OAuth2AuthorizeRequestState;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.repository.OAuth2AuthorizeRequestStateRepository;
import com.sheetconn.connector.service.ConnectorRegistryService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OAuth2AuthorizationCodeGrantFilter extends OncePerRequestFilter {
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final ConnectorRegistryService connectorRegistryService;

    private final AuthenticationProvider authenticationProvider;

    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();

    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    private final OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository;

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    private final GoogleIdTokenVerifier idTokenVerifier;

    /**
     * Constructs an {@code OAuth2AuthorizationCodeGrantFilter} using the provided
     * parameters.
     * @param clientRegistrationRepository the repository of client registrations
     * @param connectorRegistryService the authorized client repository
     * @param authenticationProvider the authentication manager
     */
    public OAuth2AuthorizationCodeGrantFilter(ClientRegistrationRepository clientRegistrationRepository,
                                              ConnectorRegistryService connectorRegistryService, AuthenticationProvider authenticationProvider,
                                              OAuth2AuthorizeRequestStateRepository authorizeRequestStateRepository,
                                              GoogleIdTokenVerifier idTokenVerifier) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(connectorRegistryService, "connectorRegistryService cannot be null");
        Assert.notNull(authenticationProvider, "authenticationManager cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.connectorRegistryService = connectorRegistryService;
        this.authenticationProvider = authenticationProvider;
        this.authorizeRequestStateRepository = authorizeRequestStateRepository;
        this.idTokenVerifier = idTokenVerifier;
    }

    /**
     * Sets the repository for stored {@link OAuth2AuthorizationRequest}'s.
     * @param authorizationRequestRepository the repository for stored
     * {@link OAuth2AuthorizationRequest}'s
     */
    public final void setAuthorizationRequestRepository(
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        Assert.notNull(authorizationRequestRepository, "authorizationRequestRepository cannot be null");
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (matchesAuthorizationResponse(request)) {
            processAuthorizationResponse(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean matchesAuthorizationResponse(HttpServletRequest request) {
        MultiValueMap<String, String> params = OAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        if (!OAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            return false;
        }
        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
                .loadAuthorizationRequest(request);
        if (authorizationRequest == null) {
            return false;
        }
        // Compare redirect_uri
        UriComponents requestUri = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request)).build();
        UriComponents redirectUri = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri()).build();
        Set<Map.Entry<String, List<String>>> requestUriParameters = new LinkedHashSet<>(
                requestUri.getQueryParams().entrySet());
        Set<Map.Entry<String, List<String>>> redirectUriParameters = new LinkedHashSet<>(
                redirectUri.getQueryParams().entrySet());
        // Remove the additional request parameters (if any) from the authorization
        // response (request)
        // before doing an exact comparison with the authorizationRequest.getRedirectUri()
        // parameters (if any)
        requestUriParameters.retainAll(redirectUriParameters);
        return Objects.equals(requestUri.getScheme(), redirectUri.getScheme())
                && Objects.equals(requestUri.getUserInfo(), redirectUri.getUserInfo())
                && Objects.equals(requestUri.getHost(), redirectUri.getHost())
                && Objects.equals(requestUri.getPort(), redirectUri.getPort())
                && Objects.equals(requestUri.getPath(), redirectUri.getPath())
                && Objects.equals(requestUriParameters.toString(), redirectUriParameters.toString());
    }

    private void processAuthorizationResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
                .removeAuthorizationRequest(request, response);
        String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
        MultiValueMap<String, String> params = OAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        String redirectUri = UrlUtils.buildFullRequestUrl(request);
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponseUtils.convert(params,
                redirectUri);
        OAuth2AuthorizationCodeAuthenticationToken authenticationRequest = new OAuth2AuthorizationCodeAuthenticationToken(
                clientRegistration, new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        OAuth2AuthorizationCodeAuthenticationToken authenticationResult;
        try {
            authenticationResult = (OAuth2AuthorizationCodeAuthenticationToken) this.authenticationProvider
                    .authenticate(authenticationRequest);
        }
        catch (OAuth2AuthorizationException ex) {
            OAuth2Error error = ex.getError();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri())
                    .queryParam(OAuth2ParameterNames.ERROR, error.getErrorCode());
            if (StringUtils.hasLength(error.getDescription())) {
                uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION, error.getDescription());
            }
            if (StringUtils.hasLength(error.getUri())) {
                uriBuilder.queryParam(OAuth2ParameterNames.ERROR_URI, error.getUri());
            }
            this.redirectStrategy.sendRedirect(request, response, uriBuilder.build().encode().toString());
            return;
        }
        // get uid from state
        String state = authenticationResult.getAuthorizationExchange().getAuthorizationRequest().getState();
        Optional<OAuth2AuthorizeRequestState> requestState = authorizeRequestStateRepository
                .findById(state);

        if(requestState.isEmpty()) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri())
                    .queryParam(OAuth2ParameterNames.ERROR, 400);
            uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION, "Request not initiated by this domain");
            this.redirectStrategy.sendRedirect(request, response, uriBuilder.build().encode().toString());
            return;
        }

        authorizeRequestStateRepository.deleteById(state);

        String uid = requestState.get().getUid();

        // Need to use generic token verifier that can verify tokens from different oauth provider
        // Check for audience as well
        Claims claims = idTokenVerifier.getClaims(
                (String) authenticationResult.getAdditionalParameters().get("id_token"));

        OAuth2AuthenticationToken authorizedToken = new OAuth2AuthenticationToken(
                claims,
                authenticationResult.getClientRegistration(),
                authenticationResult.getAuthorizationExchange(),
                authenticationResult.getAccessToken(),
                authenticationResult.getRefreshToken());

        this.connectorRegistryService.registerOAuthConnect(uid, authorizedToken);
        String redirectUrl = authorizationRequest.getRedirectUri();
        this.redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

}
