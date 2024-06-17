package onlydust.com.marketplace.project.domain.port.output;

import java.util.Set;

public interface GithubAuthenticationInfoPort {
    Set<String> getAuthorizedScopes(String accessToken);
}
