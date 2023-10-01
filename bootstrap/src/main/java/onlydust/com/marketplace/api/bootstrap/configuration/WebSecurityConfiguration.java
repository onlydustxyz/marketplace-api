package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationConfiguration;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public AuthenticationConfiguration apiSecurityConfiguration(final AuthenticationFilter authenticationFilter) {
        return new AuthenticationConfiguration(authenticationFilter);
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
    public AuthenticationService authenticationService() {
        return new AuthenticationService();
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
