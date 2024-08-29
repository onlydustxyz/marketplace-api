package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.SponsorId;

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

    Payment pay(final @NonNull RewardId rewardId,
                final @NonNull ZonedDateTime confirmedAt,
                final @NonNull Network network,
                final @NonNull String transactionHash);

    List<Payment> pay(final Set<RewardId> rewardIds);

    void cancel(final @NonNull RewardId rewardId, @NonNull Currency.Id currencyId);

    void cancel(@NonNull Payment payment);

    void confirm(Payment payment);

    boolean isPayable(RewardId rewardId, Currency.Id currencyId);

    SponsorAccountStatement delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id receiptId);

    void allocate(SponsorId from, ProgramId programId, PositiveAmount amount, Currency.Id currencyId);

    void unallocate(ProgramId from, SponsorId to, PositiveAmount amount, Currency.Id currencyId);

    void grant(ProgramId from, ProjectId to, PositiveAmount amount, Currency.Id currencyId);

    void ungrant(ProjectId projectId, ProgramId programId, @NonNull PositiveAmount amount, Currency.Id currencyId);

    Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId);

    SponsorAccountStatement updateSponsorAccount(final @NonNull SponsorAccount.Id sponsorAccountId, ZonedDateTime lockedUntil);

    List<Network> networksOf(Currency.Id currencyId, RewardId rewardId);

    Map<SponsorAccount, PositiveAmount> balancesPerOrigin(RewardId id, Currency.Id currencyId);

    Map<SponsorAccount, PositiveAmount> transferredAmountPerOrigin(RewardId id, Currency.Id currencyId);

    Deposit previewDeposit(SponsorId sponsorId, Network network, String transactionReference);

    Amount getSponsorBalance(@NonNull SponsorId sponsorId, @NonNull Currency currency);
}
