package onlydust.com.marketplace.api.bootstrap.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public AuthenticatedUser newFakeUser(UUID userId, long githubUserId, String login, String avatarUrl,
                                         boolean isAdmin) {
        final AuthUserEntity user = AuthUserEntity.builder()
                .id(userId)
                .githubUserId(githubUserId)
                .loginAtSignup(login)
                .avatarUrlAtSignup(avatarUrl)
                .isAdmin(false)
                .createdAt(new Date())
                .isAdmin(isAdmin)
                .build();
        authUserRepository.save(user);

        return authenticateUser(user);
    }

    @NonNull
    public String getImpersonationHeaderToImpersonatePierre() {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final HasuraJwtPayload.HasuraClaims hasuraClaims = objectMapper.readValue("""
                    {
                        "x-hasura-projectsLeaded": "{}",
                        "x-hasura-githubUserId": "16590657",
                        "x-hasura-odAdmin": "false",
                        "x-hasura-githubAccessToken": "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",
                        "x-hasura-allowed-roles": [
                          "me",
                          "registered_user",
                          "public"
                        ],
                        "x-hasura-default-role": "registered_user",
                        "x-hasura-user-id": "fc92397c-3431-4a84-8054-845376b630a0",
                        "x-hasura-user-is-anonymous": "false",
                        "x-hasura-login": "PierreOucif",
                        "x-hasura-avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                    }
                    """, HasuraJwtPayload.HasuraClaims.class);
            return objectMapper.writeValueAsString(hasuraClaims);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
    public AuthenticatedUser authenticateAnthony() {
        return authenticateUser(43467246L);
    }

    @NonNull
    public AuthenticatedUser authenticateOlivier() {
        return authenticateUser(595505L);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(Long githubUserId) {
        final AuthUserEntity user = authUserRepository.findByGithubUserId(githubUserId).orElseThrow();
        return authenticateUser(user);
    }

    @NonNull
    public AuthenticatedUser authenticateUser(Long githubUserId, String githubPAT) {
        final AuthUserEntity user = authUserRepository.findByGithubUserId(githubUserId).orElseThrow();
        return authenticateUser(user, githubPAT);
    }



    @NonNull
    public AuthenticatedUser authenticateUser(AuthUserEntity user) {
        return authenticateUser(user, null);
    }

    public AuthenticatedUser authenticateUser(AuthUserEntity user, String githubPAT) {
        try {
            return new AuthenticatedUser(HasuraJwtHelper.generateValidJwtFor(jwtSecret, HasuraJwtPayload.builder()
                    .iss(jwtSecret.getIssuer())
                    .claims(HasuraJwtPayload.HasuraClaims.builder()
                            .userId(user.getId())
                            .allowedRoles(List.of("me", "public", "registered_user"))
                            .githubUserId(user.getGithubUserId())
                            .avatarUrl(user.getAvatarUrlAtSignup())
                            .githubAccessToken(githubPAT)
                            .login(user.getLoginAtSignup())
                            .build())
                    .build()), user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public record AuthenticatedUser(String jwt, AuthUserEntity user) {
    }
}
