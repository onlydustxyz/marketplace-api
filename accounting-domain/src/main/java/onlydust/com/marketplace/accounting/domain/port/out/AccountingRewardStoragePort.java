package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentDetailsView;
import onlydust.com.marketplace.accounting.domain.view.BatchPaymentShortView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
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
                                        Date fromRequestedAt, Date toRequestedAt,
                                        Date fromProcessedAt, Date toProcessedAt);

    List<RewardDetailsView> findPaidRewardsToNotify();

    void markRewardsAsPaymentNotified(List<RewardId> rewardId);

    void saveAll(List<Payment> payments);

    void deletePayment(Payment.Id paymentId);

    Optional<RewardDetailsView> getReward(RewardId id);
}
