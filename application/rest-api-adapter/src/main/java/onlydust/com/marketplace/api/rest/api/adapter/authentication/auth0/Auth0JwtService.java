package onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0;

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
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Auth0JwtService implements JwtService {
    private final ObjectMapper objectMapper;
    private final UserFacadePort userFacadePort;
    private final HttpClient httpClient;
    private final Auth0Properties properties;

    @Override
    public Optional<OnlyDustAuthentication> getAuthenticationFromJwt(final String jwt,
                                                                     final String impersonationHeader) {
        try {
            final var jwtClaims = getUserInfo(jwt);
            final User user = getUserFromClaims(jwtClaims, false);

            if (impersonationHeader != null && !impersonationHeader.isEmpty()) {
                return getAuthenticationFromImpersonationHeader(user, impersonationHeader);
            }

            return Optional.of(Auth0Authentication.builder()
                    .authorities(user.getRoles().stream().map(OnlyDustGrantedAuthority::new).collect(Collectors.toList()))
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

    private Auth0JwtClaims getUserInfo(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.jwksUrl + "userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
            throw OnlyDustException.internalServerError("Unable to get user info from Auth0: [%d] %s".formatted(response.statusCode(), response.body()));

        return objectMapper.readValue(response.body(), Auth0JwtClaims.class);
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

    private Optional<OnlyDustAuthentication> getAuthenticationFromImpersonationHeader(User impersonator,
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
                .isAuthenticated(true)
                .user(impersonated)
                .principal(impersonated.getGithubUserId().toString())
                .impersonating(true)
                .impersonator(impersonator)
                .build());
    }

}
