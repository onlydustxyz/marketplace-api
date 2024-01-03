package onlydust.com.marketplace.accounting.domain;

import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CommitteeAccountingStoragePort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class CommitteeAccountingStorage implements CommitteeAccountingStoragePort {

    final List<CommitteeBalance> committeeBalances = new ArrayList<>();

    @Override
    public Optional<BigDecimal> getBalance(UUID committeeId, Currency currency) {
        return committeeBalances.stream()
                .filter(cb -> cb.committeeId.equals(committeeId) && cb.currency.equals(currency))
                .findFirst()
                .map(CommitteeBalance::amount);
    }

    @Override
    public void saveBalance(UUID committeeId, BigDecimal amount, Currency currency) {
        committeeBalances.removeIf(cb -> cb.committeeId.equals(committeeId) && cb.currency.equals(currency));
        committeeBalances.add(new CommitteeBalance(committeeId, amount, currency));
    }

    record CommitteeBalance(UUID committeeId, BigDecimal amount, Currency currency) {
    }
}
