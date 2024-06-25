package onlydust.com.marketplace.project.domain.port.output;

import java.util.Optional;

public interface GithubAuthenticationPort {
    String getGithubPersonalToken(Long githubUserId);

    Optional<String> logout(Long githubUserId);
}
