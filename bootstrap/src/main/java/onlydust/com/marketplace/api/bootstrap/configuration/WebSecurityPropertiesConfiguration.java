package onlydust.com.marketplace.api.bootstrap.configuration;

import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.ApiSecurityConfiguration;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSecurityPropertiesConfiguration {

    @Bean
    public ApiSecurityConfiguration apiSecurityConfiguration(final AuthenticationFilter authenticationFilter) {
        return new ApiSecurityConfiguration(authenticationFilter);
    }

    @Bean
    public AuthenticationFilter authenticationFilter(final HasuraProperties hasuraProperties) {
        return new AuthenticationFilter(hasuraProperties);
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
    @ConfigurationProperties("application.web.hasura")
    public HasuraProperties hasuraProperties() {
        return new HasuraProperties();
    }
}
