package onlydust.com.marketplace.project.domain.port.input;

import java.util.List;

public interface ContributionObserverPort {
    void onContributionsChanged(List<Long> repoIds);
}
