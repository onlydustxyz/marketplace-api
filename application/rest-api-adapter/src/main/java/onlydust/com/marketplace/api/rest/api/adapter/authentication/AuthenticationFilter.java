package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    public final static String BEARER_PREFIX = "Bearer ";
    public final static String IMPERSONATION_HEADER = "X-Impersonation-Claims";
    private final JwtService jwtServiceAuth0;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authorization = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String impersonationHeader = httpServletRequest.getHeader(IMPERSONATION_HEADER);
        if (nonNull(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            try {
                jwtServiceAuth0.getAuthenticationFromJwt(authorization.replace(BEARER_PREFIX, ""), impersonationHeader)
                        .ifPresentOrElse(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication),
                                SecurityContextHolder::clearContext);
            } catch (Exception e) {
                LOGGER.error("Error while authenticating user", e);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
