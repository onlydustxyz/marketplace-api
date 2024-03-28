package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Date;
import java.util.List;

public interface AccountingRewardPort {

    Page<BackofficeRewardView> getRewards(int pageIndex, int pageSize,
                                          List<RewardStatus> statuses,
                                          Date fromRequestedAt, Date toRequestedAt,
                                          Date fromProcessedAt, Date toProcessedAt);

    String exportRewardsCSV(List<RewardStatus> statuses,
                            Date fromRequestedAt, Date toRequestedAt,
                            Date fromProcessedAt, Date toProcessedAt);

    void notifyAllNewPaidRewards();

    BackofficeRewardView getReward(RewardId id);
}
