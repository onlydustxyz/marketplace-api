package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GithubUserIdentity {
    @EqualsAndHashCode.Include
    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    String email;
}
