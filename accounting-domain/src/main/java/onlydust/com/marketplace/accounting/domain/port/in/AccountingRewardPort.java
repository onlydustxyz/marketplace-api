package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;

public interface AccountingRewardPort {

    Page<RewardDetailsView> getRewards(int pageIndex, int pageSize,
                                       List<RewardStatus.Input> statuses,
                                       List<BillingProfile.Id> billingProfileIds,
                                       Date fromRequestedAt, Date toRequestedAt,
                                       Date fromProcessedAt, Date toProcessedAt);

    String exportRewardsCSV(List<RewardStatus.Input> statuses,
                            List<BillingProfile.Id> billingProfileIds,
                            Date fromRequestedAt, Date toRequestedAt,
                            Date fromProcessedAt, Date toProcessedAt);

    void notifyAllNewPaidRewards();

    RewardDetailsView getReward(RewardId id);
}
