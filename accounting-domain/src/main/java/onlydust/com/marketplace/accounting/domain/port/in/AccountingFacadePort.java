package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;

import java.time.ZonedDateTime;

public interface AccountingFacadePort {
    SponsorAccountStatement createSponsorAccount(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId,
                                                 final @NonNull PositiveAmount amountToMint,
                                                 final ZonedDateTime lockedUntil);

    SponsorAccountStatement createSponsorAccount(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId,
                                                 final @NonNull PositiveAmount amountToMint,
                                                 final ZonedDateTime lockedUntil, final @NonNull SponsorAccount.Transaction transaction);

    SponsorAccountStatement fund(final @NonNull SponsorAccount.Id sponsorAccountId, final @NonNull SponsorAccount.Transaction transaction);

    void pay(RewardId from, Currency.Id currencyId, SponsorAccount.Transaction transaction);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    void deleteTransaction(String reference);

    void increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount, Currency.Id currencyId);

    <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId);
}
