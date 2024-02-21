package onlydust.com.marketplace.project.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLinkedReposChanged;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;
import onlydust.com.marketplace.project.domain.port.output.IndexerPort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@AllArgsConstructor
public class IndexerApiOutboxConsumer implements OutboxConsumer {

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
        } else if (event instanceof UserSignedUp userSignedUp) {
            indexerPort.indexUser(userSignedUp.getGithubUserId());
        }
    }
}
