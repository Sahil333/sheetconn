package com.sheetconn.connector.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheetconn.connector.controllers.advice.ErrorCodeMap;
import com.sheetconn.connector.controllers.advice.ErrorResponse;
import com.sheetconn.connector.expceptions.InvalidAudienceException;
import com.sheetconn.connector.expceptions.InvalidTokenException;
import com.sheetconn.connector.expceptions.MissingPermissionException;
import com.sheetconn.connector.expceptions.NoTokenPresentException;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GoogleAuthenticationFilter extends OncePerRequestFilter {

    private final GoogleIdTokenVerifier idTokenVerifier;
    private final ObjectMapper mapper;

    private final UserService userService;

    public GoogleAuthenticationFilter(
            GoogleIdTokenVerifier idTokenVerifier,
            ObjectMapper mapper,
            UserService userService) {
        this.idTokenVerifier = idTokenVerifier;
        this.mapper = mapper;
        this.userService = userService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            ApplicationAuthenticationToken authenticationResult = attemptAuthentication(request, response);
            userService.addUser(authenticationResult.getClaims());
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            handleFailure(request, response, ex);
        }
    }

    private void handleFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        ErrorResponse errorResponse =
                new ErrorResponse(HttpStatus.UNAUTHORIZED,
                        ErrorCodeMap.getErrorCode(ex.getClass()),
                        ex.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        try {
            response.getWriter().append(mapper.writeValueAsString(errorResponse));
        } catch (JsonProcessingException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().append("{\"message\": \"Error while processing authentication exception\", \"errorCode\": \"")
                    .append(String.valueOf(ErrorCodeMap.getErrorCode(JsonProcessingException.class)))
                    .append("\"}");
        }
    }

    private ApplicationAuthenticationToken attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String token = getToken(request);

        if(token == null) {
            throw new NoTokenPresentException("No token is present in headers or query parameter");
        }

        try {
            Claims claims = idTokenVerifier.tokenValidation(token);
            return new ApplicationAuthenticationToken(claims, token);
        } catch (InvalidAudienceException |
                 UnsupportedJwtException |
                 MalformedJwtException |
                 SecurityException |
                 ExpiredJwtException |
                 IllegalArgumentException ex) {
            throw new InvalidTokenException(ex.getMessage());
        } catch (IncorrectClaimException | MissingClaimException ex) {
            throw new MissingPermissionException(ex.getMessage());
        }
    }

    private String getToken(HttpServletRequest request) {
        String bearerToken = null;
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7);
        } else {
            bearerToken = request.getParameter("id_token");
        }
        return bearerToken;
    }
}
