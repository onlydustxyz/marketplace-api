package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.CommitteeAccountProvider;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountProvider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class SponsorAccountingService {

    private final SponsorAccountProvider sponsorAccountProvider;
    private final CommitteeAccountProvider committeeAccountProvider;
    private final CurrencyStorage currencyStorage;

    public void receiveFrom(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var account = getOrCreateAccount(sponsorId, currency);

        account.mint(PositiveMoney.of(PositiveMoney.of(amount, currency)));
    }

    public void refundTo(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var account = getAccount(sponsorId, currency);

        account.burn(PositiveMoney.of(PositiveMoney.of(amount, currency)));
    }

    private Account getOrCreateAccount(SponsorId sponsorId, Currency currency) {
        return sponsorAccountProvider.get(sponsorId, currency)
                .orElseGet(() -> sponsorAccountProvider.create(sponsorId, currency));
    }

    private Account getAccount(SponsorId sponsorId, Currency currency) {
        return sponsorAccountProvider.get(sponsorId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No account found for sponsor %s in currency %s".formatted(sponsorId, currency)));
    }

    private Account getOrCreateAccount(CommitteeId committeeId, Currency currency) {
        return committeeAccountProvider.get(committeeId, currency)
                .orElseGet(() -> committeeAccountProvider.create(committeeId, currency));
    }

    private Account getAccount(CommitteeId committeeId, Currency currency) {
        return committeeAccountProvider.get(committeeId, currency)
                .orElseThrow(() -> OnlyDustException.notFound("No account found for committee %s in currency %s".formatted(committeeId, currency)));
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> OnlyDustException.notFound("Currency %s not found".formatted(id)));
    }

    public void allocate(SponsorId sponsorId, CommitteeId committeeId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var sponsorAccount = getAccount(sponsorId, currency);
        final var committeeAccount = getOrCreateAccount(committeeId, currency);

        sponsorAccount.send(committeeAccount, PositiveMoney.of(amount, currency));
    }

    public void unallocate(SponsorId sponsorId, CommitteeId committeeId, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var sponsorAccount = getAccount(sponsorId, currency);
        final var committeeAccount = getAccount(committeeId, currency);

        committeeAccount.refund(sponsorAccount, PositiveMoney.of(amount, currency));
    }
}
