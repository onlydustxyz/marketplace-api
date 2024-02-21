package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class GithubUserIdentity {
    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    String email;
}
