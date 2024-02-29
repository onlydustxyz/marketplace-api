package onlydust.com.marketplace.api.rest.api.adapter.authentication.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtClaims;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.AuthenticationService;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.UserRole;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Auth0OnlyDustAppAuthenticationService implements AuthenticationService {
    private final ObjectMapper objectMapper;
    private final UserFacadePort userFacadePort;

    @Override
    public Optional<Authentication> getAuthentication(@NotNull final Auth0JwtClaims userClaims,
                                                      @NotNull final String credentials,
                                                      final String impersonationHeader) {
        final var user = getUserFromClaims(userClaims, false);

        if (impersonationHeader != null && !impersonationHeader.isEmpty()) {
            return getAuthenticationFromImpersonationHeader(credentials, user, impersonationHeader);
        }

        return Optional.of(Auth0OnlyDustAppAuthentication.builder()
                .authorities(user.getRoles().stream().map(OnlyDustAppGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(credentials)
                .isAuthenticated(true)
                .user(user)
                .principal(user.getGithubUserId().toString())
                .impersonating(false)
                .build());
    }

    private User getUserFromClaims(final Auth0JwtClaims jwtClaims, final boolean isImpersonated) {
        final Long githubUserId = Long.valueOf(jwtClaims.getSub().replaceFirst("github\\|", ""));
        return this.userFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(jwtClaims.getNickname())
                .githubAvatarUrl(jwtClaims.getPicture())
                .email(jwtClaims.getEmail())
                .build(), isImpersonated);
    }

    private Optional<Authentication> getAuthenticationFromImpersonationHeader(final String credentials,
                                                                              final User impersonator,
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

        return Optional.of(Auth0OnlyDustAppAuthentication.builder()
                .authorities(impersonated.getRoles().stream().map(OnlyDustAppGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(credentials)
                .isAuthenticated(true)
                .user(impersonated)
                .principal(impersonated.getGithubUserId().toString())
                .impersonating(true)
                .impersonator(impersonator)
                .build());
    }

}
