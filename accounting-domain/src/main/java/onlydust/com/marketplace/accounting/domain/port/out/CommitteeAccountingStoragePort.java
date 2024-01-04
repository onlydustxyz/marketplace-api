package onlydust.com.marketplace.accounting.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;

public interface CommitteeAccountingStoragePort {

  Optional<BigDecimal> getBalance(CommitteeId committeeId, Currency currency);

  void saveBalance(CommitteeId committeeId, BigDecimal amount, Currency currency);
}
