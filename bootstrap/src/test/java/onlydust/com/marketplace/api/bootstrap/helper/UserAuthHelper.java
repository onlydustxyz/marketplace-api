package onlydust.com.marketplace.api.bootstrap.helper;

import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.NonNull;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class UserAuthHelper {

    @Autowired
    UserRepository userRepository;
    @Autowired
    JWTVerifier jwtVerifier;
    @Autowired
    GithubAuthenticationPort githubAuthenticationPort;

    @NonNull
    public AuthenticatedUser newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl,
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

        return authenticateUser(user);
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

        final var token = ((JwtVerifierStub) jwtVerifier).tokenFor(user.getGithubUserId(), user.getGithubLogin(),
                user.getGithubAvatarUrl(), user.getGithubEmail());

        if (githubPAT != null) {
            ((Auth0ApiClientStub) githubAuthenticationPort).withPat(user.getGithubUserId(), githubPAT);
        }

        return new AuthenticatedUser(token, user);
    }


    public record AuthenticatedUser(String jwt, UserEntity user) {
    }
}
