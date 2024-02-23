package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;

import java.util.List;

public interface PayoutPreferenceStoragePort {
    List<PayoutPreferenceView> findAllByUserId(UserId userId);

    boolean hasUserReceivedSomeRewardsOnProject(UserId userId, ProjectId projectId);

    void save(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId);
}
