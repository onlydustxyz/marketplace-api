package onlydust.com.marketplace.accounting.domain.port.out;

public interface WalletValidator<T> {
    boolean isValid(T wallet);
}
