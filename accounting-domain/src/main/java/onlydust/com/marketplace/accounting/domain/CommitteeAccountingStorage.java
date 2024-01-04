package onlydust.com.marketplace.accounting.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CommitteeAccountingStoragePort;

@Getter
public class CommitteeAccountingStorage implements CommitteeAccountingStoragePort {

  final List<CommitteeBalance> committeeBalances = new ArrayList<>();

  @Override
  public Optional<BigDecimal> getBalance(CommitteeId committeeId, Currency currency) {
    return committeeBalances.stream()
        .filter(cb -> cb.committeeId.equals(committeeId) && cb.currency.equals(currency))
        .findFirst()
        .map(CommitteeBalance::amount);
  }

  @Override
  public void saveBalance(CommitteeId committeeId, BigDecimal amount, Currency currency) {
    committeeBalances.removeIf(cb -> cb.committeeId.equals(committeeId) && cb.currency.equals(currency));
    committeeBalances.add(new CommitteeBalance(committeeId, amount, currency));
  }

  record CommitteeBalance(CommitteeId committeeId, BigDecimal amount, Currency currency) {

  }
}
