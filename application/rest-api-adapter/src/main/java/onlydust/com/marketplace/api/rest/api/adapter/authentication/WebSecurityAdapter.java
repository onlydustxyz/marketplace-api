package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class WebSecurityAdapter extends WebSecurityConfigurerAdapter {

  private final AuthenticationFilter authenticationFilter;
  private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
  private final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().cors()
        .and().csrf().disable()
        .authorizeRequests()
        .antMatchers("/api/v1/me/**").hasAuthority(UserRole.USER.name())
        .antMatchers("/api/v1/events/**").hasAuthority(UserRole.INTERNAL_SERVICE.name())
        .antMatchers("/bo/v1/**").hasAuthority(UserRole.INTERNAL_SERVICE.name())
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
        .anyRequest().authenticated()
        .and()
        .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
        .addFilterAfter(apiKeyAuthenticationFilter, AuthenticationFilter.class)
        .exceptionHandling().authenticationEntryPoint(delegatedAuthenticationEntryPoint);
  }

}
