package onlydust.com.marketplace.api.domain.job;

public class OutboxSkippingException extends RuntimeException {
    public OutboxSkippingException(final String message) {
        super(message);
    }
}
