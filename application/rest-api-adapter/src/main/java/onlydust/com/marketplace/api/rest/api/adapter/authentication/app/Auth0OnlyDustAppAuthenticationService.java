package onlydust.com.marketplace.api.rest.api.adapter.authentication.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtClaims;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.AuthenticationService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Auth0OnlyDustAppAuthenticationService implements AuthenticationService {
    private final ObjectMapper objectMapper;
    private final AppUserFacadePort appUserFacadePort;

    @Override
    public Optional<Authentication> getAuthentication(@NonNull final Auth0JwtClaims userClaims,
                                                      @NonNull final String credentials,
                                                      final String impersonationHeader) {
        final var user = getUserFromClaims(userClaims, false);

        if (impersonationHeader != null && !impersonationHeader.isEmpty()) {
            return getAuthenticationFromImpersonationHeader(credentials, user, impersonationHeader);
        }

        return Optional.of(Auth0OnlyDustAppAuthentication.builder()
                .authorities(user.roles().stream().map(OnlyDustAppGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(credentials)
                .isAuthenticated(true)
                .user(user)
                .principal(user.githubUserId().toString())
                .impersonating(false)
                .build());
    }

    private AuthenticatedUser getUserFromClaims(final Auth0JwtClaims jwtClaims, final boolean isImpersonated) {
        final Long githubUserId = Long.valueOf(jwtClaims.getSub().replaceFirst("github\\|", ""));
        return this.appUserFacadePort.getUserByGithubIdentity(GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .login(jwtClaims.getNickname())
                .avatarUrl(jwtClaims.getPicture())
                .email(jwtClaims.getEmail())
                .build(), isImpersonated);
    }

    private Optional<Authentication> getAuthenticationFromImpersonationHeader(final String credentials,
                                                                              final AuthenticatedUser impersonator,
                                                                              final String impersonationHeader) {
        if (!impersonator.roles().contains(AuthenticatedUser.Role.ADMIN)) {
            LOGGER.warn("User {} is not allowed to impersonate", impersonator.login());
            return Optional.empty();
        }
        final Auth0JwtClaims claims;
        try {
            claims = objectMapper.readValue(impersonationHeader, Auth0JwtClaims.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid impersonation header: {}", impersonationHeader);
            return Optional.empty();
        }

        final var impersonated = getUserFromClaims(claims, true);

        LOGGER.info("User {} is impersonating {}", impersonator, impersonated);

        return Optional.of(Auth0OnlyDustAppAuthentication.builder()
                .authorities(impersonated.roles().stream().map(OnlyDustAppGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(credentials)
                .isAuthenticated(true)
                .user(impersonated)
                .principal(impersonated.githubUserId().toString())
                .impersonating(true)
                .impersonator(impersonator)
                .build());
    }

}
