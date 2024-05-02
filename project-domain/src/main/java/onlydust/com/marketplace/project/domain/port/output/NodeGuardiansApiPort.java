package onlydust.com.marketplace.project.domain.port.output;

import java.util.Optional;

public interface NodeGuardiansApiPort {
    Optional<Integer> getContributorLevel(String githubLogin);
}
