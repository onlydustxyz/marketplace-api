package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;

import java.util.List;

public interface PayoutPreferenceFacadePort {

    // TODO: move to read-api
    List<PayoutPreferenceView> getPayoutPreferences(UserId userId);

    void setPayoutPreference(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId);
}
