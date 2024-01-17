package onlydust.com.marketplace.api.bootstrap.configuration;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtVerifier;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.net.http.HttpClient;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint(final HandlerExceptionResolver handlerExceptionResolver) {
        return new DelegatedAuthenticationEntryPoint(handlerExceptionResolver);
    }

    @Bean
    public WebSecurityAdapter apiSecurityConfiguration(final AuthenticationFilter authenticationFilter,
                                                       final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                                                       final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint) {
        return new WebSecurityAdapter(authenticationFilter, apiKeyAuthenticationFilter,
                delegatedAuthenticationEntryPoint);
    }

    @Bean
    public JWTVerifier jwtVerifier(final Auth0Properties auth0Properties) {
        return new Auth0JwtVerifier(auth0Properties);
    }

    @Bean
    public JwtService jwtServiceAuth0(final ObjectMapper objectMapper,
                                      final JWTVerifier jwtVerifier,
                                      final UserFacadePort userFacadePort,
                                      final Auth0Properties auth0Properties) {
        return new Auth0JwtService(objectMapper, jwtVerifier, userFacadePort, HttpClient.newHttpClient(), auth0Properties);
    }

    @Bean
    public AuthenticationFilter authenticationFilter(final JwtService jwtServiceAuth0) {
        return new AuthenticationFilter(jwtServiceAuth0);
    }

    @Bean
    public AuthenticationContext authenticationContext() {
        return new SpringAuthenticationContext();
    }

    @Bean
    public AuthenticationService authenticationService(final AuthenticationContext authenticationContext) {
        return new AuthenticationService(authenticationContext);
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(final ApiKeyAuthenticationService apiKeyAuthenticationService) {
        return new ApiKeyAuthenticationFilter(apiKeyAuthenticationService);
    }

    @Bean
    public ApiKeyAuthenticationService apiKeyAuthenticationService(final ApiKeyAuthenticationService.Config apiKeyAuthenticationConfig) {
        return new ApiKeyAuthenticationService(apiKeyAuthenticationConfig);
    }

    @Bean
    @ConfigurationProperties("application.web.cors")
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }

    @Bean
    @ConfigurationProperties("application.web.auth0")
    public Auth0Properties auth0Properties() {
        return new Auth0Properties();
    }

    @Data
    public static class WebCorsProperties {
        private String[] hosts;
    }
}
