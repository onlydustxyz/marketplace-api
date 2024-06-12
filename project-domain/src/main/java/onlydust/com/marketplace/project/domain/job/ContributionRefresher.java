package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (event instanceof OnContributionChanged onContributionChanged)
            contributionObserverPort.onContributionsChanged(onContributionChanged.repoId());

        else
            LOGGER.warn("Event type {} not handled", event.getClass().getSimpleName());
    }
}
