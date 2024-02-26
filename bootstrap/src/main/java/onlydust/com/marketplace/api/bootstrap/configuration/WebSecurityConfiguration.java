package onlydust.com.marketplace.api.bootstrap.configuration;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtVerifier;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0UserInfoService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationService;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
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
                                                       final ApiKeyAuthenticationFilter indexerApiKeyAuthenticationFilter,
                                                       final ApiKeyAuthenticationFilter backOfficeApiKeyAuthenticationFilter,
                                                       final QueryParamTokenAuthenticationFilter queryParamTokenAuthenticationFilter,
                                                       final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint) {
        return new WebSecurityAdapter(authenticationFilter, indexerApiKeyAuthenticationFilter, backOfficeApiKeyAuthenticationFilter,
                queryParamTokenAuthenticationFilter,
                delegatedAuthenticationEntryPoint);
    }

    @Bean
    public JWTVerifier jwtVerifier(final Auth0Properties auth0Properties) {
        return new Auth0JwtVerifier(auth0Properties);
    }

    @Bean
    public Auth0UserInfoService auth0UserInfoService(final ObjectMapper objectMapper,
                                                     final JWTVerifier jwtVerifier,
                                                     final Auth0Properties auth0Properties) {
        return new Auth0UserInfoService(objectMapper, HttpClient.newHttpClient(), auth0Properties, jwtVerifier);
    }

    @Bean
    public JwtService jwtServiceAuth0(final ObjectMapper objectMapper,
                                      final UserFacadePort userFacadePort,
                                      final JWTVerifier jwtVerifier,
                                      final Auth0UserInfoService auth0UserInfoService) {
        return new Auth0JwtService(objectMapper, userFacadePort, auth0UserInfoService, jwtVerifier);
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
    public ApiKeyAuthenticationFilter indexerApiKeyAuthenticationFilter(final ApiKeyAuthenticationService indexerApiKeyAuthenticationService) {
        return new ApiKeyAuthenticationFilter(indexerApiKeyAuthenticationService);
    }

    @Bean
    public ApiKeyAuthenticationService indexerApiKeyAuthenticationService(final ApiKeyAuthenticationService.Config indexerApiKeyAuthenticationConfig) {
        return new ApiKeyAuthenticationService(indexerApiKeyAuthenticationConfig);
    }

    @Bean
    public ApiKeyAuthenticationFilter backOfficeApiKeyAuthenticationFilter(final ApiKeyAuthenticationService backOfficeApiKeyAuthenticationService) {
        return new ApiKeyAuthenticationFilter(backOfficeApiKeyAuthenticationService);
    }

    @Bean
    public ApiKeyAuthenticationService backOfficeApiKeyAuthenticationService(final ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig) {
        return new ApiKeyAuthenticationService(backOfficeApiKeyAuthenticationConfig);
    }

    @Bean
    public QueryParamTokenAuthenticationFilter queryParamTokenAuthenticationFilter(final QueryParamTokenAuthenticationService queryParamTokenAuthenticationService) {
        return new QueryParamTokenAuthenticationFilter(queryParamTokenAuthenticationService);
    }

    @Bean
    public QueryParamTokenAuthenticationService queryParamTokenAuthenticationService(final QueryParamTokenAuthenticationService.Config queryParamTokenAuthenticationConfig) {
        return new QueryParamTokenAuthenticationService(queryParamTokenAuthenticationConfig);
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
    
    @Bean
    @ConfigurationProperties("application.web.machine-to-machine")
    public ApiKeyAuthenticationService.Config indexerApiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }

    @Bean
    @ConfigurationProperties("application.web.back-office")
    public ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }

    @Bean
    @ConfigurationProperties("application.web.back-office-invoice-token")
    public QueryParamTokenAuthenticationService.Config queryParamTokenAuthenticationConfig() {
        return new QueryParamTokenAuthenticationService.Config();
    }
}
