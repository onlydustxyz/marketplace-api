package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    public final static String BEARER_PREFIX = "Bearer ";
    private final HasuraJwtService hasuraJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (nonNull(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            SecurityContextHolder.getContext().setAuthentication(hasuraJwtService.getAuthenticationFromJwt(authorization.replace(BEARER_PREFIX, "")));
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
