package onlydust.com.marketplace.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GithubUserIdentity {
    Long githubUserId;
    String githubLogin;
    String githubAvatarUrl;
    String email;
}
