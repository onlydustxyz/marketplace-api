package onlydust.com.marketplace.user.domain.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.user.domain.event.UserSignedUp;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Slf4j
@AllArgsConstructor
public class IndexerApiUserOutboxConsumer implements OutboxConsumer {

    private final IndexerPort indexerPort;

    @Override
    public void process(Event event) {
        callIndexerApi(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void callIndexerApi(Event event) {
        if (event instanceof UserSignedUp userSignedUp) {
            indexerPort.indexUser(userSignedUp.getGithubUserId());
        }
    }
}
