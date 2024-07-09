package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

public interface PayoutPreferenceStoragePort {

    boolean hasUserReceivedSomeRewardsOnProject(UserId userId, ProjectId projectId);

    void save(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId);
}
