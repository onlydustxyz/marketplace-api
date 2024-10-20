package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.ContributionUUID;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnContributionChanged;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;

@Slf4j
@AllArgsConstructor
public class ContributionRefresher implements OutboxConsumer {
    private final ContributionObserverPort contributionObserverPort;

    @Override
    public void process(Event event) {
        if (event instanceof OnContributionChanged onContributionChanged) {
            if (onContributionChanged.contributionUUID() == null) {
                LOGGER.warn("Old-format event {} skipped", onContributionChanged);
                return;
            }
            contributionObserverPort.onContributionsChanged(onContributionChanged.repoId(),
                    ContributionUUID.of(onContributionChanged.contributionUUID()));

        } else {
            LOGGER.debug("Event type {} not handled", event.getClass().getSimpleName());
        }
    }
}
