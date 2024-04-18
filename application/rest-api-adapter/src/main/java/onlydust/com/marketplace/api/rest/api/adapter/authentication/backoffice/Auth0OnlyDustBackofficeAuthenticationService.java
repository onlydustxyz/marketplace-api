package onlydust.com.marketplace.api.rest.api.adapter.authentication.backoffice;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtClaims;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.AuthenticationService;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import onlydust.com.marketplace.user.domain.port.input.BackofficeUserFacadePort;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Auth0OnlyDustBackofficeAuthenticationService implements AuthenticationService {
    private final BackofficeUserFacadePort backofficeUserFacadePort;

    @Override
    public Optional<Authentication> getAuthentication(@NonNull final Auth0JwtClaims userClaims,
                                                      @NonNull final String credentials,
                                                      final String impersonationHeader) {

        final BackofficeUser user = backofficeUserFacadePort.getUserByIdentity(BackofficeUser.Identity.builder()
                .email(userClaims.getEmail())
                .name(userClaims.getName())
                .avatarUrl(userClaims.getPicture())
                .build());

        return Optional.of(Auth0OnlyDustBackofficeAuthentication.builder()
                .authorities(user.roles().stream().map(OnlyDustBackofficeGrantedAuthority::new).collect(Collectors.toList()))
                .credentials(credentials)
                .isAuthenticated(true)
                .user(user)
                .principal(user.email())
                .build());
    }

}
