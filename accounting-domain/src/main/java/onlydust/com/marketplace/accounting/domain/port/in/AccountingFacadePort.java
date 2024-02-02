package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.Transaction;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface AccountingFacadePort {
    Ledger createLedger(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId, final @NonNull PositiveAmount amountToMint,
                        final ZonedDateTime lockedUntil);

    Ledger createLedger(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId, final @NonNull PositiveAmount amountToMint,
                        final ZonedDateTime lockedUntil, final @NonNull Ledger.Transaction transaction);

    Ledger.Transaction.Id fund(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network, ZonedDateTime lockedUntil);

    Ledger.Transaction.Id withdraw(SponsorId sponsorId, PositiveAmount amount, Currency.Id currencyId, Network network);

    void pay(RewardId from, Currency.Id currencyId, Network network);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    void delete(Ledger.Transaction.Id transactionId);

    <To> void mint(To to, PositiveAmount amount, Currency.Id currencyId);

    <From> Collection<Transaction> burn(From from, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId);
}
