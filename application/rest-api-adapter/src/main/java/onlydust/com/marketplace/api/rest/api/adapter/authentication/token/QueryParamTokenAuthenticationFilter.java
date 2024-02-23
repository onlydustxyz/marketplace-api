package onlydust.com.marketplace.api.rest.api.adapter.authentication.token;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class QueryParamTokenAuthenticationFilter extends OncePerRequestFilter {
    private final QueryParamTokenAuthenticationService authenticationService;

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
