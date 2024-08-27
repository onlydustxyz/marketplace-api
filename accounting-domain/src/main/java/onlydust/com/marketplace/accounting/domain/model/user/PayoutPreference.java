package onlydust.com.marketplace.accounting.domain.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.model.UserId;

@Getter
@Builder
public class PayoutPreference {
    @NonNull
    UserId userId;
    @NonNull
    BillingProfile.Id billingProfileId;
    @NonNull
    ProjectId projectId;
}
