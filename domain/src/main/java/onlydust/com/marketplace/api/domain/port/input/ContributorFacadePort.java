package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Contributor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.UUID;

public interface ContributorFacadePort {
    Pair<List<Contributor>, List<Contributor>> searchContributors(UUID projectId, String login);
}
