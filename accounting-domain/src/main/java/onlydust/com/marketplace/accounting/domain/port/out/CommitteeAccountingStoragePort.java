package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeAccountingStoragePort {
    Optional<BigDecimal> getBalance(UUID committeeId, Currency currency);

    void saveBalance(UUID committeeId, BigDecimal amount, Currency currency);
}
