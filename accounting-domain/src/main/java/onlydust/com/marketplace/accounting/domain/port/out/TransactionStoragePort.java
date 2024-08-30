package onlydust.com.marketplace.accounting.domain.port.out;

public interface TransactionStoragePort {
    boolean exists(String transactionReference);
}
