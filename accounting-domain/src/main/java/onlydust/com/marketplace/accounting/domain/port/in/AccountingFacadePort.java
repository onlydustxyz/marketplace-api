package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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

    SponsorAccountStatement deleteTransaction(SponsorAccount.Id sponsorAccountId, String reference);

    SponsorAccountStatement increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount);

    <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId);

    Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId);

    Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId);

    List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId);

    SponsorAccountStatement updateSponsorAccount(final @NonNull SponsorAccount.Id sponsorAccountId, ZonedDateTime lockedUntil);

    List<PayableReward> getPayableRewards();
}
