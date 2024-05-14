package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountingRewardStoragePort {

    Optional<Payment> findPayment(Payment.Id batchPaymentId);

    void savePayment(Payment updatedPayment);

    Page<BatchPaymentShortView> findPayments(int pageIndex, int pageSize, Set<Payment.Status> statuses);

    List<BatchPaymentShortView> findPaymentsByIds(Set<Payment.Id> paymentIds);

    Optional<BatchPaymentDetailsView> findPaymentDetailsById(Payment.Id batchPaymentId);

    Page<RewardDetailsView> findRewards(int pageIndex, int pageSize,
                                        @NonNull Set<RewardStatus.Input> statuses,
                                        @NonNull List<BillingProfile.Id> billingProfileIds,
                                        @NonNull List<GithubUserId> recipients,
                                        Date fromRequestedAt, Date toRequestedAt,
                                        Date fromProcessedAt, Date toProcessedAt);

    EarningsView getEarnings(@NonNull Set<RewardStatus.Input> statuses,
                             @NonNull List<GithubUserId> recipientIds,
                             @NonNull List<BillingProfile.Id> billingProfileIds,
                             @NonNull List<ProjectId> projectIds,
                             Date fromRequestedAt, Date toRequestedAt,
                             Date fromProcessedAt, Date toProcessedAt);

    List<RewardDetailsView> findPaidRewardsToNotify();

    void markRewardsAsPaymentNotified(List<RewardId> rewardId);

    void saveAll(List<Payment> payments);

    void deletePayment(Payment.Id paymentId);

    Optional<RewardDetailsView> getReward(RewardId id);

    Optional<ShortRewardDetailsView> getShortReward(RewardId rewardId);

    void updateBillingProfileFromRecipientPayoutPreferences(RewardId rewardId);
}
