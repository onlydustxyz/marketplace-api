package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtService;
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
    public HasuraJwtService hasuraJwtService(final JwtSecret jwtSecret) {
        return new HasuraJwtService(jwtSecret);
    }

    @Bean
    public AuthenticationFilter authenticationFilter(final HasuraJwtService hasuraJwtService) {
        return new AuthenticationFilter(hasuraJwtService);
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

    @Data
    public static class WebCorsProperties {
        private String[] hosts;
    }

    @Bean
    @ConfigurationProperties("application.web.hasura.secret")
    public JwtSecret jwtSecret() {
        return new JwtSecret();
    }
}
