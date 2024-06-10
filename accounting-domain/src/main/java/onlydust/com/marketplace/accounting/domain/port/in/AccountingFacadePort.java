package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
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

    void pay(final @NonNull RewardId rewardId, final @NonNull ZonedDateTime confirmedAt, final @NonNull Network network, final @NonNull String transactionHash);

    List<Payment> pay(final Set<RewardId> rewardIds);

    void cancel(final @NonNull RewardId rewardId, @NonNull Currency.Id currencyId);

    void cancel(@NonNull Payment payment);

    void confirm(Payment payment);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    SponsorAccountStatement delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id receiptId);

    void allocate(SponsorId from, ProjectId to, PositiveAmount amount, Currency.Id currencyId);

    void allocate(SponsorAccount.Id from, ProjectId to, PositiveAmount amount, Currency.Id currencyId);

    void unallocate(ProjectId from, SponsorAccount.Id to, PositiveAmount amount, Currency.Id currencyId);

    void unallocate(ProjectId from, SponsorId to, PositiveAmount amount, Currency.Id currencyId);

    Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId);

    // TODO: move to read-api
    List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId);

    SponsorAccountStatement updateSponsorAccount(final @NonNull SponsorAccount.Id sponsorAccountId, ZonedDateTime lockedUntil);

    // TODO: move to read-api
    Page<HistoricalTransaction> transactionHistory(@NonNull SponsorId sponsorId,
                                                   @NonNull HistoricalTransaction.Filters filters,
                                                   @NonNull Integer pageIndex,
                                                   @NonNull Integer pageSize,
                                                   @NonNull HistoricalTransaction.Sort sort,
                                                   @NonNull SortDirection direction);

    List<Network> networksOf(Currency.Id currencyId, RewardId rewardId);

    Map<SponsorAccount, PositiveAmount> balancesPerOrigin(RewardId id, Currency.Id currencyId);

    Map<SponsorAccount, PositiveAmount> transferredAmountPerOrigin(RewardId id, Currency.Id currencyId);
}
