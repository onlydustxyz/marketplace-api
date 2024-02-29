package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.JwtService;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class Auth0JwtService implements JwtService {
    private final Auth0UserInfoService userInfoService;
    private final JWTVerifier jwtVerifier;

    private final AuthenticationService appAuthenticationService;
    private final AuthenticationService backofficeAuthenticationService;

    @Override
    public Optional<Authentication> getAuthenticationFromJwt(final String accessToken,
                                                             final String impersonationHeader) {
        try {
            jwtVerifier.verify(accessToken);
            final var userClaims = userInfoService.getUserInfo(accessToken);
            return switch (userClaims.connection()) {
                case GITHUB -> appAuthenticationService.getAuthentication(userClaims, accessToken, impersonationHeader);
                case GOOGLE -> backofficeAuthenticationService.getAuthentication(userClaims, accessToken, impersonationHeader);
            };
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize Jwt token", e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.warn("Invalid Jwt token", e);
            return Optional.empty();
        }
    }
}
