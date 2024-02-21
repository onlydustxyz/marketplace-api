package onlydust.com.marketplace.project.domain.job;

public class OutboxSkippingException extends RuntimeException {
    public OutboxSkippingException(final String message) {
        super(message);
    }
}
