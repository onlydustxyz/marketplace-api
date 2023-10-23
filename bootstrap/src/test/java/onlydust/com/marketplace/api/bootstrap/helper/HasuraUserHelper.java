package onlydust.com.marketplace.api.bootstrap.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.AuthUserEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.AuthUserRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.jwt.JwtSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Profile({"hasura_auth"})
@Component
public class HasuraUserHelper {

    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    JwtSecret jwtSecret;

    @NonNull
    public AuthenticatedUser newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl) throws JsonProcessingException {
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .loginAtSignup(login)
                .avatarUrlAtSignup(avatarUrl)
                .isAdmin(false)
                .createdAt(new Date())
                .build();
        authUserRepository.save(user);

        return authenticateUser(user);
    }

    @NonNull
    public AuthenticatedUser authenticatePierre() throws JsonProcessingException {
        return authenticateUser(16590657L);
    }

    @NonNull
    public AuthenticatedUser authenticateAnthony() throws JsonProcessingException {
        return authenticateUser(43467246L);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(Long githubUserId) throws JsonProcessingException {
        final AuthUserEntity user = authUserRepository.findByGithubUserId(githubUserId).orElseThrow();
        return authenticateUser(user);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(AuthUserEntity user) throws JsonProcessingException {
        return new AuthenticatedUser(HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                .iss(jwtSecret.getIssuer())
                .claims(HasuraJwtPayload.HasuraClaims.builder()
                        .userId(user.getId())
                        .allowedRoles(List.of("me", "public", "registered_user"))
                        .githubUserId(user.getGithubUserId())
                        .avatarUrl(user.getAvatarUrlAtSignup())
                        .login(user.getLoginAtSignup())
                        .build())
                .build()), user);
    }

    public record AuthenticatedUser(String jwt, AuthUserEntity user) {
    }
}
