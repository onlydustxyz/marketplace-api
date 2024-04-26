package onlydust.com.marketplace.accounting.domain.job;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.MailPort;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@AllArgsConstructor
public class MailNotificationOutboxConsumer implements OutboxConsumer {

    private final MailPort mailPort;

    @Override
    public void process(Event event) {
        sendMail(event);
    }

    @Retryable(maxAttempts = 6, backoff = @Backoff(delay = 500, multiplier = 2))
    private void sendMail(Event event) {
        mailPort.send(event);
    }
}
