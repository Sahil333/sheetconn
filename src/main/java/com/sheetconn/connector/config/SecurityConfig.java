package com.sheetconn.connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheetconn.connector.oauth.GoogleAuthenticationFilter;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           GoogleAuthenticationFilter googleAuthenticationFilter,
                                           @Qualifier("authorizeGrantFilter") FilterRegistrationBean<OncePerRequestFilter> authorizeGrantFilter) throws Exception {
        http.csrf((AbstractHttpConfigurer::disable));
        http.authorizeHttpRequests(
                (authz) -> authz
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(googleAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authorizeGrantFilter.getFilter(), GoogleAuthenticationFilter.class);
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public GoogleAuthenticationFilter googleIdTokenFilter(GoogleIdTokenVerifier idTokenVerifier, ObjectMapper mapper) {
        return new GoogleAuthenticationFilter(idTokenVerifier, mapper, userService);
    }
}
