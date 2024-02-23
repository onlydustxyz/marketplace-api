package onlydust.com.marketplace.kernel.model.bank;

import lombok.Builder;
import lombok.NonNull;


@Builder
public record BankAccount(@NonNull String bic, @NonNull String accountNumber) {
}
