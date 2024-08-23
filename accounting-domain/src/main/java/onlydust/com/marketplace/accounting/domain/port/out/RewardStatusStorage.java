package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RewardStatusStorage {
    void persist(@NonNull RewardStatusData rewardStatusData);

    void updateInvoiceReceivedAt(@NonNull RewardId rewardId, ZonedDateTime invoiceReceivedAt);

    void updatePaidAt(@NonNull RewardId rewardId, ZonedDateTime paidAt);

    void updateUsdAmount(@NonNull RewardId rewardId, ConvertedAmount usdAmount);

    void updateAccountingData(@NonNull RewardId rewardId,
                              @NonNull Boolean sponsorHasEnoughFund,
                              ZonedDateTime unlockDate,
                              @NonNull Set<Network> networks,
                              ConvertedAmount usdAmount);

    Optional<RewardStatusData> get(final @NonNull RewardId rewardId);

    List<RewardStatusData> get(final @NonNull List<RewardId> rewardIds);

    void delete(RewardId rewardId);

    List<RewardStatusData> notRequested();

    List<RewardStatusData> notRequested(BillingProfile.Id billingProfileId);
}
