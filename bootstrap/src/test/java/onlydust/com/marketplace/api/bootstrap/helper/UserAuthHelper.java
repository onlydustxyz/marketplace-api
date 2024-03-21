package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import onlydust.com.marketplace.project.domain.model.UserRole;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@AllArgsConstructor
public class UserAuthHelper {
    UserRepository userRepository;
    JWTVerifier jwtVerifier;
    GithubAuthenticationPort githubAuthenticationPort;
    WireMockServer auth0WireMockServer;

    @NonNull
    public AuthenticatedUser newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl,
                                         boolean isAdmin) {
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
                .roles(isAdmin ? new UserRole[]{UserRole.USER, UserRole.ADMIN} : new UserRole[]{UserRole.USER})
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
    public String getImpersonationHeaderToImpersonatePierre() {
        return "{\"sub\":\"github|%d\"}".formatted(16590657L);
    }

    @NonNull
    public AuthenticatedUser authenticatePierre() {
        return authenticateUser(16590657L, null);
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
    public AuthenticatedUser authenticateUser(UserEntity user) {
        return authenticateUser(user, null);
    }

    public AuthenticatedUser authenticateUser(UserEntity user, String githubPAT) {
        final var token = ((JwtVerifierStub) jwtVerifier).tokenFor(user.getGithubUserId());

        if (githubPAT != null) {
            ((Auth0ApiClientStub) githubAuthenticationPort).withPat(user.getGithubUserId(), githubPAT);
        }

        return new AuthenticatedUser(token, user);
    }


    public record AuthenticatedUser(String jwt, UserEntity user) {
    }
}
