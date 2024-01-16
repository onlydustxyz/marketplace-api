package onlydust.com.marketplace.api.bootstrap.helper;

import lombok.NonNull;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.ClaimsProvider;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.auth0.Auth0JwtClaims;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Auth0ClaimsProviderStub implements ClaimsProvider<Auth0JwtClaims> {

    private final Map<String, Auth0JwtClaims> claimsPerAccessToken = new HashMap<>();

    public String tokenFor(String githubWithUserId, String login, String avatarUrl, String email) {
        final var claims = Auth0JwtClaims.builder()
                .githubWithUserId(githubWithUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .build();

        final String token = "token-for-%s".formatted(githubWithUserId);
        claimsPerAccessToken.put(token, claims);
        return token;
    }

    public String tokenFor(Long githubUserId, String login, String avatarUrl, String email) {
        return tokenFor("github|%d".formatted(githubUserId), login, avatarUrl, email);
    }

    public String tokenFor(Auth0JwtClaims claims) {
        return tokenFor(claims.getGithubWithUserId(), claims.getGithubLogin(), claims.getGithubAvatarUrl(),
                claims.getEmail());
    }

    @Override
    public Auth0JwtClaims getClaimsFromAccessToken(@NonNull String accessToken) {
        return Optional.ofNullable(claimsPerAccessToken.get(accessToken))
                .orElseThrow(() -> OnlyDustException.internalServerError("Invalid token"));
    }
}
