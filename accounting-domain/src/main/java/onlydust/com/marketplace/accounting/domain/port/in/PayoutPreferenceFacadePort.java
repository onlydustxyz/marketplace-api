package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

public interface PayoutPreferenceFacadePort {

    void setPayoutPreference(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId);
}
