package onlydust.com.marketplace.project.domain.port.output;

import java.util.Set;

public interface GithubAuthenticationInfoPort {
    void logout(String accessToken);

    Set<String> getAuthorizedScopes(String accessToken);
}
