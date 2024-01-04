package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CommitteeAccountingStoragePort {
    Optional<BigDecimal> getBalance(CommitteeId committeeId, Currency currency);

    void saveBalance(CommitteeId committeeId, BigDecimal amount, Currency currency);
}
