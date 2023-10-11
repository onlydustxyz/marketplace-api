package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
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
    private final DelegatedAuthenticationEntryPoint delegatedAuthenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().cors()
                .and().authorizeRequests()
                .antMatchers("/api/v1/me/**").hasAuthority("me")
                .antMatchers(HttpMethod.GET, "/api/v1/projects/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/github/**").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.GET, "/v3/api-docs").permitAll()
                .antMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
                .anyRequest().authenticated()
                .and().addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(delegatedAuthenticationEntryPoint);
    }

}
