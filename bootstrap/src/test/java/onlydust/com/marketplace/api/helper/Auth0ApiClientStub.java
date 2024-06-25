package onlydust.com.marketplace.api.helper;

import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class Auth0ApiClientStub implements GithubAuthenticationPort {

    private final Map<Long, String> pats = Collections.synchronizedMap(new HashMap<>());

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
        return Optional.ofNullable(pats.get(githubUserId))
                .orElseThrow(() -> internalServerError(("No Github personal token for user %d in Auth0ApiClientStub").formatted(githubUserId)));
    }

    @Override
    public void logout(Long githubUserId) {
        pats.remove(githubUserId);
    }

    public void withPat(Long githubUserId, String pat) {
        pats.put(githubUserId, pat);
    }
}
