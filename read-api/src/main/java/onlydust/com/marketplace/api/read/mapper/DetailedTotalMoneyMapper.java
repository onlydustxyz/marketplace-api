package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.DetailedTotalMoneyTotalPerCurrencyInner;
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
        if (stats == null)
            return null;

        final var usdTotal = stats.stream()
                .map(s -> s.usdAmount(amountSupplier.apply(s)))
                .filter(Objects::nonNull)
                .reduce(BigDecimal::add)
                .orElse(ZERO);

        return new DetailedTotalMoney()
                .totalPerCurrency(stats.stream()
                        .sorted(comparing(c -> c.currency().name()))
                        .map(s -> s.toMoney(amountSupplier.apply(s), usdTotal))
                        .sorted(comparing(DetailedTotalMoneyTotalPerCurrencyInner::getUsdEquivalent, nullsLast(naturalOrder())).reversed())
                        .toList())
                .totalUsdEquivalent(prettyUsd(usdTotal));
    }
}
