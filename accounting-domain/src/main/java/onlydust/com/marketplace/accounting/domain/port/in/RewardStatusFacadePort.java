package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccountStatement;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;

import java.util.List;

public interface RewardStatusFacadePort {
    void refreshRewardsUsdEquivalents();

    void refreshRelatedRewardsStatuses(SponsorAccountStatement sponsorAccount);

    void refreshRewardsUsdEquivalentOf(BillingProfile.Id billingProfileId);

    void refreshRewardsUsdEquivalentOf(List<RewardId> rewardIds);

    void refreshRewardsUsdEquivalentOf(RewardId rewardId);

    void create(AccountBookFacade accountBookFacade, RewardId rewardId);

    void delete(RewardId rewardId);
}
