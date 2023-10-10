package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationContext;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.SpringAuthenticationContext;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public onlydust.com.marketplace.api.rest.api.adapter.authentication.WebSecurityConfiguration apiSecurityConfiguration(final AuthenticationFilter authenticationFilter) {
        return new onlydust.com.marketplace.api.rest.api.adapter.authentication.WebSecurityConfiguration(authenticationFilter);
    }

    @Bean
    public Auth0JwtService auth0JwtService(final JwtSecret jwtSecret) {
        return new Auth0JwtService(jwtSecret);
    }

    @Bean
    public AuthenticationFilter authenticationFilter(final Auth0JwtService auth0JwtService) {
        return new AuthenticationFilter(auth0JwtService);
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
    @ConfigurationProperties("application.web.hasura.secret")
    public JwtSecret jwtSecret() {
        return new JwtSecret();
    }

    @Data
    public static class WebCorsProperties {
        private String[] hosts;
    }
}
