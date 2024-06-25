package onlydust.com.marketplace.api.configuration;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.app.Auth0OnlyDustAppAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtVerifier;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0Properties;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0UserInfoService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice.Auth0OnlyDustBackofficeAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationService;
import onlydust.com.marketplace.project.domain.port.input.GithubUserPermissionsFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.net.http.HttpClient;

import static onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role.*;
import static onlydust.com.marketplace.user.domain.model.BackofficeUser.Role.*;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    public DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint(final HandlerExceptionResolver handlerExceptionResolver) {
        return new DelegatedAuthenticationEntryPoint(handlerExceptionResolver);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain filterChain(HttpSecurity http, final AuthenticationFilter authenticationFilter,
                                           final ApiKeyAuthenticationFilter indexerApiKeyAuthenticationFilter,
                                           final ApiKeyAuthenticationFilter backOfficeApiKeyAuthenticationFilter,
                                           final QueryParamTokenAuthenticationFilter queryParamTokenAuthenticationFilter,
                                           final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint) throws Exception {
        http
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(antMatcher("/bo/v1/external/**")).hasAnyAuthority(UNSAFE_INTERNAL_SERVICE.name(), BO_READER.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/bo/v1/**")).hasAnyAuthority(BO_READER.name())
                                .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/bo/v1/**")).hasAnyAuthority(BO_READER.name())
                                .requestMatchers(antMatcher(HttpMethod.HEAD, "/bo/v1/**")).hasAnyAuthority(BO_READER.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/hackathons/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/hackathons/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/hackathons/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/hackathons/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/committees/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/committees/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/committees/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/committees/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/committees/**/allocations")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/rewards/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/rewards/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/rewards/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/rewards/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/sponsors")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/sponsors/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/sponsors/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/sponsors/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/sponsors/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/sponsor-accounts/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/sponsor-accounts/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/sponsor-accounts/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/sponsor-accounts/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/projects/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/projects/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/projects/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/projects/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/currencies/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/currencies/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PATCH, "/bo/v1/currencies/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/currencies/**")).hasAnyAuthority(BO_FINANCIAL_ADMIN.name())

                                .requestMatchers(antMatcher(HttpMethod.GET, "/bo/v1/project-categories")).hasAnyAuthority(BO_READER.name())
                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/project-categories")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/project-category-suggestions/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/bo/v1/project-categories/**")).hasAnyAuthority(BO_READER.name())
                                .requestMatchers(antMatcher(HttpMethod.POST, "/bo/v1/project-categories/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.PUT, "/bo/v1/project-categories/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())
                                .requestMatchers(antMatcher(HttpMethod.DELETE, "/bo/v1/project-categories/**")).hasAnyAuthority(BO_MARKETING_ADMIN.name())

                                .requestMatchers(antMatcher("/api/v1/me/**")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher("/api/v1/billing-profiles/**")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher("/api/v1/events/**")).hasAuthority(INTERNAL_SERVICE.name())
                                .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/projects/**")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/users/search")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/committees/*")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/projects/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/users/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v2/users/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/github/**")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/technologies")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/hackathons/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/ecosystems/*/contributors")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/ecosystems/*/projects")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/ecosystems/slug/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/ecosystems")).hasAuthority(USER.name())
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v2/ecosystems")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/project-categories")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/public-activity")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/swagger-ui.html")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/v3/api-docs/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/swagger-ui/**")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/actuator/health")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/version")).permitAll()
                                .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/sumsub/webhook")).permitAll()
                                .anyRequest().authenticated())

                .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(indexerApiKeyAuthenticationFilter, AuthenticationFilter.class)
                .addFilterAfter(backOfficeApiKeyAuthenticationFilter, AuthenticationFilter.class)
                .addFilterAfter(queryParamTokenAuthenticationFilter, AuthenticationFilter.class)
                .exceptionHandling(
                        (exceptionHandling) -> exceptionHandling.authenticationEntryPoint(delegatedAuthenticationEntryPoint)
                );
        return http.build();
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
    public JwtService jwtServiceAuth0(final JWTVerifier jwtVerifier,
                                      final Auth0UserInfoService auth0UserInfoService,
                                      final Auth0OnlyDustAppAuthenticationService appAuthenticationService,
                                      final Auth0OnlyDustBackofficeAuthenticationService backofficeAuthenticationService
    ) {
        return new Auth0JwtService(auth0UserInfoService, jwtVerifier, appAuthenticationService, backofficeAuthenticationService);
    }

    @Bean
    public Auth0OnlyDustAppAuthenticationService appAuthenticationService(final ObjectMapper objectMapper,
                                                                          final UserFacadePort userFacadePort) {
        return new Auth0OnlyDustAppAuthenticationService(objectMapper, userFacadePort);
    }

    @Bean
    public Auth0OnlyDustBackofficeAuthenticationService backofficeAuthenticationService(final BackofficeUserFacadePort backofficeUserFacadePort) {
        return new Auth0OnlyDustBackofficeAuthenticationService(backofficeUserFacadePort);
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
    public AuthenticatedAppUserService authenticatedAppUserService(final AuthenticationContext authenticationContext,
                                                                   final GithubUserPermissionsFacadePort githubUserPermissionsFacadePort) {
        return new AuthenticatedAppUserService(authenticationContext, githubUserPermissionsFacadePort);
    }

    @Bean
    public AuthenticatedBackofficeUserService authenticatedBackofficeUserService(final AuthenticationContext authenticationContext) {
        return new AuthenticatedBackofficeUserService(authenticationContext);
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
    @ConfigurationProperties(value = "application.web.cors", ignoreUnknownFields = false)
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }

    @Bean
    @ConfigurationProperties(value = "application.web.auth0", ignoreUnknownFields = false)
    public Auth0Properties auth0Properties() {
        return new Auth0Properties();
    }

    @Data
    public static class WebCorsProperties {
        private String[] hosts;
    }

    @Bean
    @ConfigurationProperties(value = "application.web.machine-to-machine", ignoreUnknownFields = false)
    public ApiKeyAuthenticationService.Config indexerApiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }

    @Bean
    @ConfigurationProperties(value = "application.web.back-office", ignoreUnknownFields = false)
    public ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig() {
        return new ApiKeyAuthenticationService.Config();
    }

    @Bean
    @ConfigurationProperties(value = "application.web.back-office-invoice-token", ignoreUnknownFields = false)
    public QueryParamTokenAuthenticationService.Config queryParamTokenAuthenticationConfig() {
        return new QueryParamTokenAuthenticationService.Config();
    }
}
