package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
    public final static String IMPERSONATION_HEADER = "X-Impersonation-Claims";
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String impersonationHeader = httpServletRequest.getHeader(IMPERSONATION_HEADER);
        if (nonNull(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            jwtService.getAuthenticationFromJwt(authorization.replace(BEARER_PREFIX, ""), impersonationHeader).ifPresent(authentication -> {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
