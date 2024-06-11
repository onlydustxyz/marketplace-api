package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.event.OnNewContribution;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.port.input.ContributionObserverPort;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
@AllArgsConstructor
public class IndexingEventConsumer implements OutboxConsumer {
    private final ContributionObserverPort contributionObserverPort;

    @Override
    public void process(Event event) {
        if (event instanceof OnNewContribution onNewContribution)
            contributionObserverPort.onContributionsChanged(onNewContribution.repoIds().stream().toList());
        else
            throw internalServerError("Unknown event type: %s".formatted(event.getClass().getSimpleName()));
    }
}
