package onlydust.com.marketplace.api.helper;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Auth0ApiClientStub implements GithubAuthenticationPort {

    private final Map<Long, String> pats = Collections.synchronizedMap(new HashMap<>());

    @Override
    public String getGithubPersonalToken(Long githubUserId) {
        final var pat = pats.get(githubUserId);
        if (pat == null) {
            throw OnlyDustException.internalServerError(("No Github personal token for user %d in Auth0ApiClientStub").formatted(githubUserId));
        }
        return pat;
    }

    @Override
    public void logout(Long githubUserId) {
        pats.remove(githubUserId);
    }

    public void withPat(Long githubUserId, String pat) {
        pats.put(githubUserId, pat);
    }
}
