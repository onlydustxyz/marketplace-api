package onlydust.com.marketplace.api.infura;

public class NotAnERC20TransferException extends RuntimeException {
    public NotAnERC20TransferException(String message) {
        super(message);
    }
}
