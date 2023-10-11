package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Auth0JwtService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JWTVerifier jwtVerifier;
    private final UserFacadePort userFacadePort;

    public Auth0JwtService(JWTVerifier jwtVerifier, UserFacadePort userFacadePort) {
        this.userFacadePort = userFacadePort;
        this.jwtVerifier = jwtVerifier;
    }


    private static Auth0Authentication getUnauthenticatedWithExceptionFor(final int status,
                                                                          final String message,
                                                                          final Exception exception
    ) {
        return Auth0Authentication.builder()
                .onlydustException(OnlydustException
                        .builder()
                        .status(status)
                        .message(message)
                        .rootException(exception)
                        .build())
                .build();
    }

    public Optional<Auth0Authentication> getAuthenticationFromJwt(final String jwt) {
        try {
            final DecodedJWT decodedJwt = this.jwtVerifier.verify(jwt);
            final Auth0JwtClaims jwtClaims = objectMapper.readValue(Base64.getUrlDecoder().decode(decodedJwt.getPayload()),
                    Auth0JwtClaims.class);
            final Long githubUserId = Long.valueOf(jwtClaims.getGithubWithUserId().replaceFirst("github\\|", ""));
            final User user = this.userFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
                    .githubUserId(githubUserId)
                    .githubLogin(jwtClaims.getGithubLogin())
                    .githubAvatarUrl(jwtClaims.getGithubAvatarUrl())
                    .build());

            return Optional.of(Auth0Authentication.builder()
                    .authorities(user.getPermissions().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                    .credentials(decodedJwt)
                    .isAuthenticated(true)
                    .user(user)
                    .principal(decodedJwt.getSubject())
                    .build());
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize Jwt token", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.debug("Invalid Jwt token", e);
            return Optional.empty();
        }

    }

}
