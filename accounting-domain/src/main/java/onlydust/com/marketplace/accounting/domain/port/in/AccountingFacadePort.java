package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountingFacadePort {
    SponsorAccountStatement createSponsorAccountWithInitialAllowance(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId,
                                                                     final ZonedDateTime lockedUntil, final @NonNull PositiveAmount allowance);

    SponsorAccountStatement createSponsorAccountWithInitialBalance(final @NonNull SponsorId sponsorId, final @NonNull Currency.Id currencyId,
                                                                   final ZonedDateTime lockedUntil, final @NonNull SponsorAccount.Transaction transaction);

    SponsorAccountStatement fund(final @NonNull SponsorAccount.Id sponsorAccountId, final @NonNull SponsorAccount.Transaction transaction);

    SponsorAccountStatement increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount);

    void createReward(ProjectId from, RewardId to, PositiveAmount amount, Currency.Id currencyId);

    void pay(final @NonNull RewardId rewardId, final @NonNull SponsorAccount.PaymentReference paymentReference);

    List<BatchPayment> pay(final Set<RewardId> rewardIds);

    void cancel(final @NonNull RewardId rewardId, @NonNull Currency.Id currencyId);

    void cancel(@NonNull BatchPayment.Id paymentId, @NonNull Currency.Id currencyId);

    void confirm(BatchPayment payment);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    SponsorAccountStatement delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id receiptId);

    void allocate(SponsorAccount.Id from, ProjectId to, PositiveAmount amount, Currency.Id currencyId);

    void unallocate(ProjectId from, SponsorAccount.Id to, PositiveAmount amount, Currency.Id currencyId);

    Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId);

    Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId);

    List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId);

    SponsorAccountStatement updateSponsorAccount(final @NonNull SponsorAccount.Id sponsorAccountId, ZonedDateTime lockedUntil);

    List<PayableReward> getPayableRewards();

    Page<HistoricalTransaction> transactionHistory(SponsorId sponsorId, Integer pageIndex, Integer pageSize);

    List<Network> networksOf(Currency.Id currencyId, RewardId rewardId);
}
