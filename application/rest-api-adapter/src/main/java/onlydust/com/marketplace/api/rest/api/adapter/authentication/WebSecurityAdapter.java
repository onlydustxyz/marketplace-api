package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationFilter;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import static onlydust.com.marketplace.user.domain.model.BackofficeUser.Role.BO_ADMIN;
import static onlydust.com.marketplace.user.domain.model.BackofficeUser.Role.BO_READER;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class WebSecurityAdapter {

    private final AuthenticationFilter authenticationFilter;
    private final ApiKeyAuthenticationFilter indexerApiKeyAuthenticationFilter;
    private final ApiKeyAuthenticationFilter backOfficeApiKeyAuthenticationFilter;
    private final QueryParamTokenAuthenticationFilter queryParamTokenAuthenticationFilter;
    private final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) ->

                        authorize.requestMatchers("/bo/v1/external/**").hasAnyAuthority(AuthenticatedUser.Role.UNSAFE_INTERNAL_SERVICE.name(), BO_READER.name())
                                .requestMatchers(HttpMethod.GET, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(), BO_READER.name())
                                .requestMatchers(HttpMethod.OPTIONS, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(),
                                        BO_READER.name())
                                .requestMatchers(HttpMethod.HEAD, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(), BO_READER.name())
                                .requestMatchers(HttpMethod.POST, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                                .requestMatchers(HttpMethod.PUT, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                                .requestMatchers(HttpMethod.PATCH, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                                .requestMatchers(HttpMethod.DELETE, "/bo/v1/**").hasAnyAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name(),
                                        BO_ADMIN.name())

                                .requestMatchers("/api/v1/me/**").hasAuthority(AuthenticatedUser.Role.USER.name())
                                .requestMatchers("/api/v1/billing-profiles/**").hasAuthority(AuthenticatedUser.Role.USER.name())
                                .requestMatchers("/api/v1/events/**").hasAuthority(AuthenticatedUser.Role.INTERNAL_SERVICE.name())
                                .requestMatchers(HttpMethod.POST, "/api/v1/projects/**").hasAuthority(AuthenticatedUser.Role.USER.name())
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/search").hasAuthority(AuthenticatedUser.Role.USER.name())
                                .requestMatchers(HttpMethod.GET, "/api/v1/projects/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/github/**").hasAuthority(AuthenticatedUser.Role.USER.name())
                                .requestMatchers(HttpMethod.GET, "/api/v1/technologies").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/hackathons/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
                                .requestMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/version").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/sumsub/webhook").permitAll()
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

}
