package onlydust.com.marketplace.api.bootstrap.configuration;

import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Data;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtVerifier;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint(final HandlerExceptionResolver handlerExceptionResolver) {
        return new DelegatedAuthenticationEntryPoint(handlerExceptionResolver);
    }

    @Bean
    public WebSecurityAdapter apiSecurityConfiguration(final AuthenticationFilter authenticationFilter, final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint) {
        return new WebSecurityAdapter(authenticationFilter, delegatedAuthenticationEntryPoint);
    }

    @Bean
    @Profile("!hasura_auth")
    public JWTVerifier jwtVerifier(final Auth0Properties auth0Properties) {
        return new Auth0JwtVerifier(auth0Properties);
    }

    @Bean
    @Profile("!hasura_auth")
    public JwtService jwtServiceAuth0(final JWTVerifier jwtVerifier, final UserFacadePort userFacadePort) {
        return new Auth0JwtService(jwtVerifier, userFacadePort);
    }

    @Bean
    @Profile("hasura_auth")
    public JwtService jwtServiceHasura(final JwtSecret jwtSecret) {
        return new HasuraJwtService(jwtSecret);
    }

    @Bean
    public AuthenticationFilter authenticationFilter(final JwtService jwtService) {
        return new AuthenticationFilter(jwtService);
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
    @ConfigurationProperties("application.web.cors")
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }

    @Bean
    @Profile("hasura_auth")
    @ConfigurationProperties("application.web.hasura.secret")
    public JwtSecret jwtSecret() {
        return new JwtSecret();
    }

    @Bean
    @Profile("!hasura_auth")
    @ConfigurationProperties("application.web.auth0")
    public Auth0Properties auth0Properties() {
        return new Auth0Properties();
    }

    @Data
    public static class WebCorsProperties {
        private String[] hosts;
    }
}
