package onlydust.com.marketplace.api.rest.api.adapter.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClaims {
    UUID userId;
    String login;
    Long githubUserId;
    String avatarUrl;

    Boolean isAnOnlydustAdmin;
    String projectsLeaded;
}
