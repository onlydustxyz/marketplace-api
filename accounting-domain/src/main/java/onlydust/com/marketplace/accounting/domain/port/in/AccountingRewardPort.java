package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.EarningsView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;

import java.util.Date;
import java.util.List;

public interface AccountingRewardPort {

    EarningsView getEarnings(List<RewardStatus.Input> statuses,
                             List<GithubUserId> recipientIds,
                             List<BillingProfile.Id> billingProfileIds,
                             List<ProjectId> projectIds,
                             Date fromRequestedAt, Date toRequestedAt,
                             Date fromProcessedAt, Date toProcessedAt);

    void notifyAllNewPaidRewards();

    RewardDetailsView getReward(RewardId id);
}
