package onlydust.com.marketplace.project.domain.port.input;

public interface ContributionObserverPort {
    void onContributionsChanged(Long repoId);
}
