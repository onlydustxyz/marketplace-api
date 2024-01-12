package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.JwtService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustAuthentication;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.OnlyDustGrantedAuthority;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Auth0JwtService implements JwtService {
    private final ObjectMapper objectMapper;
    private final JWTVerifier jwtVerifier;
    private final UserFacadePort userFacadePort;

    public Optional<OnlyDustAuthentication> getAuthenticationFromJwt(final String jwt,
                                                                     final String impersonationHeader) {
        try {
            final DecodedJWT decodedJwt = this.jwtVerifier.verify(jwt);
            final Auth0JwtClaims jwtClaims =
                    objectMapper.readValue(Base64.getUrlDecoder().decode(decodedJwt.getPayload()),
                            Auth0JwtClaims.class);
            final User user = getUserFromClaims(jwtClaims, false);

            if (impersonationHeader != null && !impersonationHeader.isEmpty()) {
                return getAuthenticationFromImpersonationHeader(decodedJwt, user, impersonationHeader);
            }

            return Optional.of(Auth0Authentication.builder()
                    .authorities(user.getRoles().stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
                    .credentials(decodedJwt)
                    .isAuthenticated(true)
                    .user(user)
                    .principal(user.getGithubUserId().toString())
                    .impersonating(false)
                    .build());
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize Jwt token", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.debug("Invalid Jwt token", e);
            return Optional.empty();
        }

    }

    private User getUserFromClaims(Auth0JwtClaims jwtClaims, boolean isImpersonated) {
        final Long githubUserId = Long.valueOf(jwtClaims.getGithubWithUserId().replaceFirst("github\\|", ""));
        return this.userFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(jwtClaims.getGithubLogin())
                .githubAvatarUrl(jwtClaims.getGithubAvatarUrl())
                .email(jwtClaims.getEmail())
                .build(), isImpersonated);
    }

    private Optional<OnlyDustAuthentication> getAuthenticationFromImpersonationHeader(DecodedJWT decodedJwt,
                                                                                      User impersonator,
                                                                                      final String impersonationHeader) {
        if (!impersonator.getRoles().contains(UserRole.ADMIN)) {
            LOGGER.warn("User {} is not allowed to impersonate", impersonator.getGithubLogin());
            return Optional.empty();
        }
        final Auth0JwtClaims claims;
        try {
            claims = objectMapper.readValue(impersonationHeader, Auth0JwtClaims.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid impersonation header: {}", impersonationHeader);
            return Optional.empty();
        }

        final User impersonated = getUserFromClaims(claims, true);

        LOGGER.info("User {} is impersonating {}", impersonator, impersonated);

        return Optional.of(Auth0Authentication.builder()
                .authorities(impersonated.getRoles().stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(decodedJwt)
                .isAuthenticated(true)
                .user(impersonated)
                .principal(impersonated.getGithubUserId().toString())
                .impersonating(true)
                .impersonator(impersonator)
                .build());
    }

}
