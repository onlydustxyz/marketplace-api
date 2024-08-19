package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;
import static java.util.Comparator.*;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

public interface DetailedTotalMoneyMapper {
    static <T extends ProgramTransactionStat> DetailedTotalMoney map(Collection<T> stats, Function<T, BigDecimal> amountSupplier) {
        return stats == null ? null : new DetailedTotalMoney()
                .totalPerCurrency(stats.stream()
                        .map(s -> s.toMoney(amountSupplier.apply(s)))
                        .sorted(comparing(Money::getUsdEquivalent, nullsLast(naturalOrder())).reversed()).toList())
                .totalUsdEquivalent(prettyUsd(stats.stream()
                        .map(s -> s.usdAmount(amountSupplier.apply(s)))
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal::add)
                        .orElse(ZERO)));
    }
}
