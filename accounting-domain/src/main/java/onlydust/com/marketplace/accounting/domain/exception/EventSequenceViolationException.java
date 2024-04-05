package onlydust.com.marketplace.accounting.domain.exception;

public class EventSequenceViolationException extends Exception {
    public EventSequenceViolationException(String message) {
        super(message);
    }

    public EventSequenceViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}