package onlydust.com.marketplace.kernel.jobs;

public class OutboxSkippingException extends RuntimeException {
    public OutboxSkippingException(final String message) {
        super(message);
    }
}
