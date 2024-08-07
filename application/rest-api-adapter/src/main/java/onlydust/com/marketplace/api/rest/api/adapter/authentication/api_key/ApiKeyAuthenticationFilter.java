package onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyAuthenticationService authenticationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        authenticationService.getAuthentication(httpServletRequest).ifPresent(
                authentication -> SecurityContextHolder.getContext().setAuthentication(authentication)
        );

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
