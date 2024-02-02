package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface AccountingFacadePort {
    Ledger createLedger(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId, final @NonNull PositiveAmount amountToMint,
                        final ZonedDateTime lockedUntil);

    Ledger createLedger(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId, final @NonNull PositiveAmount amountToMint,
                        final ZonedDateTime lockedUntil, final @NonNull Ledger.Transaction transaction);

    Ledger fund(final @NonNull Ledger.Id sponsorAccountId, final @NonNull Ledger.Transaction transaction);

    void pay(RewardId from, Currency.Id currencyId, Ledger.Transaction transaction);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    void delete(Ledger.Transaction.Id transactionId);

    void mint(Ledger.Id sponsorAccountId, PositiveAmount amount, Currency.Id currencyId);

    Collection<AccountBook.Transaction> burn(Ledger.Id sponsorAccountId, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId);
}
