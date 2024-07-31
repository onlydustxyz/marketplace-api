package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@AllArgsConstructor
public class IndexerApiProjectOutboxConsumer implements OutboxConsumer {

    private final IndexerPort indexerPort;

    @Override
    public void process(Event event) {
        callIndexerApi(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void callIndexerApi(Event event) {
        if (event instanceof ProjectLinkedReposChanged projectLinkedReposChanged) {
            indexerPort.onRepoLinkChanged(projectLinkedReposChanged.getLinkedRepoIds(),
                    projectLinkedReposChanged.getUnlinkedRepoIds());
        }
    }
}
