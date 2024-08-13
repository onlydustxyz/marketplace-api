package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.contract.model.DetailedTotalMoney;
import onlydust.com.marketplace.api.contract.model.Money;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStatReadEntity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Function;

import static java.util.Comparator.comparing;

public interface DetailedTotalMoneyMapper {
    // TODO factorize
    static DetailedTotalMoney map(Collection<ProgramTransactionStatReadEntity> stats, Function<ProgramTransactionStatReadEntity, BigDecimal> amountSupplier) {
        return new DetailedTotalMoney()
                .totalPerCurrency(stats.stream().map(s -> s.toMoney(amountSupplier.apply(s))).sorted(comparing(Money::getUsdEquivalent).reversed()).toList())
                .totalUsdEquivalent(stats.stream().map(s -> s.usdAmount(amountSupplier.apply(s))).reduce(BigDecimal::add).orElse(null));
    }

    static DetailedTotalMoney mapMonthly(Collection<ProgramTransactionMonthlyStatReadEntity> stats,
                                         Function<ProgramTransactionMonthlyStatReadEntity, BigDecimal> amountSupplier) {
        return new DetailedTotalMoney()
                .totalPerCurrency(stats.stream().map(s -> s.toMoney(amountSupplier.apply(s))).sorted(comparing(Money::getUsdEquivalent).reversed()).toList())
                .totalUsdEquivalent(stats.stream().map(s -> s.usdAmount(amountSupplier.apply(s))).reduce(BigDecimal::add).orElse(null));
    }
}
