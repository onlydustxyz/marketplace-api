package onlydust.com.marketplace.api.helper;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.backoffice.BackofficeUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BackofficeUserRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForEmptyJson;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@AllArgsConstructor
public class UserAuthHelper {
    UserRepository userRepository;
    BackofficeUserRepository backofficeUserRepository;
    JWTVerifier jwtVerifier;
    GithubAuthenticationPort githubAuthenticationPort;
    WireMockServer auth0WireMockServer;
    WireMockServer githubWireMockServer;

    @NonNull
    public AuthenticatedUser newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl, boolean isAdmin) {
        return authenticateUser(signUpUser(userId, githubUserId, login, avatarUrl, isAdmin));
    }

    public UserEntity signUpUser(UUID userId, long githubUserId, String login, String avatarUrl,
                                 boolean isAdmin) {
        final UserEntity user = UserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .githubEmail("%d@foo.org".formatted(githubUserId))
                .roles(isAdmin ?
                        new onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role[]{onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role.USER,
                                onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role.ADMIN} :
                        new onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role[]{onlydust.com.marketplace.kernel.model.AuthenticatedUser.Role.USER})
                .createdAt(new Date())
                .lastSeenAt(new Date())
                .build();
        userRepository.save(user);

        mockAuth0UserInfo(user);
        return user;
    }

    public void mockAuth0UserInfo(UserEntity user) {
        mockAuth0UserInfo(user.getGithubUserId(), user.getGithubLogin(), user.getGithubLogin(),
                user.getGithubAvatarUrl(), user.getGithubEmail());
    }

    public void mockAuth0UserInfo(BackofficeUserEntity user) {
        mockAuth0UserInfo("google-oauth2|" + user.getEmail(), user.getName(), user.getName(), user.getAvatarUrl(), user.getEmail());
    }

    public void mockAuth0UserInfo(Long githubUserId, String login) {
        mockAuth0UserInfo(githubUserId, login, login + "@gmail.com");
    }

    public void mockAuth0UserInfo(Long githubUserId, String login, String email) {
        mockAuth0UserInfo(githubUserId, login, login, "https://avatars.githubusercontent.com/u/%d?v=4".formatted(githubUserId), email);
    }

    public void mockAuth0UserInfo(Long githubUserId, String login, String name, String avatarUrl, String email) {
        mockAuth0UserInfo("github|%d".formatted(githubUserId), login, name, avatarUrl, email);
    }

    public void mockAuth0UserInfo(String sub, String nickname, String name, String avatarUrl, String email) {
        auth0WireMockServer.stubFor(
                get(urlPathEqualTo("/"))
                        .withHeader("Authorization", containing("Bearer token-for-%s".formatted(sub)))
                        .willReturn(ok()
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                            "sub": "%s",
                                            "nickname": "%s",
                                            "name": "%s",
                                            "picture": "%s",
                                            "updated_at": "2023-12-11T12:33:51Z",
                                            "email": "%s",
                                            "email_verified": true
                                        }
                                        """.formatted(sub, nickname, name, avatarUrl, email)
                                )));
    }

    @NonNull
    public AuthenticatedUser authenticatePierre() {
        return authenticateUser(16590657L);
    }

    @NonNull
    public AuthenticatedUser authenticatePierre(String githubPAT) {
        return authenticateUser(16590657L, githubPAT);
    }

    @NonNull
    public AuthenticatedUser authenticateHayden() {
        return authenticateUser(5160414L);
    }

    @NonNull
    public AuthenticatedUser authenticateAnthony() {
        return authenticateUser(43467246L);
    }

    @NonNull
    public AuthenticatedUser authenticateOlivier() {
        return authenticateUser(595505L);
    }

    @NonNull
    public AuthenticatedUser authenticateGregoire() {
        return authenticateUser(8642470L, null);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(Long githubUserId) {
        final var user = userRepository.findByGithubUserId(githubUserId).orElseThrow();
        return authenticateUser(user);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(Long githubUserId, String githubPAT) {
        final var user = userRepository.findByGithubUserId(githubUserId).orElseThrow();
        return authenticateUser(user, githubPAT);
    }

    @NonNull
    public AuthenticatedBackofficeUser authenticateCamille() {
        return authenticateBackofficeUser("admin@onlydust.xyz", List.of(BackofficeUser.Role.BO_READER, BackofficeUser.Role.BO_FINANCIAL_ADMIN));
    }

    @NonNull
    public AuthenticatedBackofficeUser authenticateEmilie() {
        return authenticateBackofficeUser("emilie@onlydust.xyz", List.of(BackofficeUser.Role.BO_READER, BackofficeUser.Role.BO_MARKETING_ADMIN));
    }

    @NonNull
    public AuthenticatedBackofficeUser authenticateBackofficeUser(String email, final List<BackofficeUser.Role> roles) {
        final var user = backofficeUserRepository.findByEmail(email)
                .map(backofficeUserEntity -> updateBackofficeUserRole(backofficeUserEntity, roles))
                .orElseGet(() -> backofficeUserRepository.save(
                        new BackofficeUserEntity(UUID.randomUUID(), email, "name-%s".formatted(email), "avatarUrl-%s".formatted(email),
                                roles.toArray(new BackofficeUser.Role[0]), new Date(), new Date())));
        final var token = ((JwtVerifierStub) jwtVerifier).googleTokenFor(email);
        mockAuth0UserInfo(user);
        return new AuthenticatedBackofficeUser(token, user);
    }

    public BackofficeUserEntity updateBackofficeUserRole(final BackofficeUserEntity backofficeUserEntity, final List<BackofficeUser.Role> roles) {
        backofficeUserEntity.setRoles(roles.toArray(new BackofficeUser.Role[0]));
        return backofficeUserRepository.save(backofficeUserEntity);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(UserEntity user) {
        return authenticateUser(user, "github-pat-for-%s".formatted(user.getGithubUserId()));
    }

    public AuthenticatedUser authenticateUser(UserEntity user, String githubPAT) {
        final var token = ((JwtVerifierStub) jwtVerifier).tokenFor(user.getGithubUserId());

        if (githubPAT != null) {
            ((Auth0ApiClientStub) githubAuthenticationPort).withPat(user.getGithubUserId(), githubPAT);
            githubWireMockServer.stubFor(WireMock.get(urlEqualTo("/"))
                    .withHeader("Authorization", equalTo("Bearer %s".formatted(githubPAT)))
                    .willReturn(okForEmptyJson().withHeader("x-oauth-scopes", "public_repo")));
        }

        return new AuthenticatedUser(token, user);
    }


    public record AuthenticatedUser(String jwt, UserEntity user) {
    }

    public record AuthenticatedBackofficeUser(String jwt, BackofficeUserEntity user) {
    }
}