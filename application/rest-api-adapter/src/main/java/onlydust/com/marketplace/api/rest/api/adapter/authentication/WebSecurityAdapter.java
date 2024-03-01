package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationFilter;
import onlydust.com.marketplace.project.domain.model.UserRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import static onlydust.com.marketplace.user.domain.model.BackofficeUser.Role.BO_ADMIN;
import static onlydust.com.marketplace.user.domain.model.BackofficeUser.Role.BO_READER;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class WebSecurityAdapter extends WebSecurityConfigurerAdapter {

    private final AuthenticationFilter authenticationFilter;
    private final ApiKeyAuthenticationFilter indexerApiKeyAuthenticationFilter;
    private final ApiKeyAuthenticationFilter backOfficeApiKeyAuthenticationFilter;
    private final QueryParamTokenAuthenticationFilter queryParamTokenAuthenticationFilter;
    private final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().cors()
                .and().csrf().disable()
                .authorizeRequests()

                .antMatchers("/bo/v1/external/**").hasAnyAuthority(UserRole.UNSAFE_INTERNAL_SERVICE.name(), BO_READER.name())
                .antMatchers(HttpMethod.GET, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_READER.name())
                .antMatchers(HttpMethod.OPTIONS, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_READER.name())
                .antMatchers(HttpMethod.HEAD, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_READER.name())
                .antMatchers(HttpMethod.POST, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                .antMatchers(HttpMethod.PATCH, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_ADMIN.name())
                .antMatchers(HttpMethod.DELETE, "/bo/v1/**").hasAnyAuthority(UserRole.INTERNAL_SERVICE.name(), BO_ADMIN.name())

                .antMatchers("/api/v1/me/**").hasAuthority(UserRole.USER.name())
                .antMatchers("/api/v1/billing-profiles/**").hasAuthority(UserRole.USER.name())
                .antMatchers("/api/v1/events/**").hasAuthority(UserRole.INTERNAL_SERVICE.name())
                .antMatchers(HttpMethod.POST, "/api/v1/projects/**").hasAuthority(UserRole.USER.name())
                .antMatchers(HttpMethod.GET, "/api/v1/users/search").hasAuthority(UserRole.USER.name())
                .antMatchers(HttpMethod.GET, "/api/v1/projects/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/github/**").hasAuthority(UserRole.USER.name())
                .antMatchers(HttpMethod.GET, "/api/v1/technologies").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").permitAll()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/version").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/sumsub/webhook").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
                .addFilterAfter(indexerApiKeyAuthenticationFilter, AuthenticationFilter.class)
                .addFilterAfter(backOfficeApiKeyAuthenticationFilter, AuthenticationFilter.class)
                .addFilterAfter(queryParamTokenAuthenticationFilter, AuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(delegatedAuthenticationEntryPoint);
    }

}
